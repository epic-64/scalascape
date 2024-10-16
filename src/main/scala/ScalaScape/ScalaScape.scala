package ScalaScape

import ScalaScape.components.*
import ScalaScape.utils.LanternBimbo
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import com.googlecode.lanterna.screen.{Screen, TerminalScreen}
import com.googlecode.lanterna.terminal.Terminal

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

@main def main(args: String*): Unit = {
  val forceTerminal = args.contains("--terminal")
  val game          = new ScalaScape(forceTerminal)
  game.run()
}

class SkillDisplay:
  def draw(graphics: TextGraphics, state: GameState, position: Position): Unit =
    state.activeSkill match {
      case Some(skill: Woodcutting) => draw(skill, graphics, position)
      case Some(skill: Quarrying)   => draw(skill, graphics, position)
      case _                        => graphics.putString(position.x, 1, "No active skill")
    }
  end draw

  private def draw(skill: Skill, graphics: TextGraphics, position: Position): Unit =
    val x  = position.x
    val y  = position.y
    val pb = ProgressBarParameters

    def headerStart = TerminalParagraph(
      List(
        TerminalString(s"${skill.name} (${skill.level} / 99)", Position(x, y), WHITE),
        TerminalString("----------------------------------------", Position(x, y + 1), WHITE)
      )
    )

    def asciiArt: TerminalParagraph = TerminalParagraph(skill.getAsciiArt(Position(x, y + 2)))

    def headerEnd = TerminalParagraph(
      List(
        TerminalString("----------------------------------------", Position(x, y + 12), WHITE)
      )
    )

    def xpBar = TerminalParagraph(
      List(TerminalString(s"XP Progress: ${skill.xp} / ${skill.xpForNextLevel}", Position(x, y + 13), WHITE))
        ++ ProgressBar.from(pb(40, skill.progressToNextLevel, Position(x, y + 14), BLUE_BRIGHT))
    )

    def actionBar = TerminalParagraph(
      List(
        TerminalString(s"Action Progress: ETA: ", Position(x, y + 16), WHITE),
        TerminalString(f"${skill.remainingDuration}%1.1f", Position(x + 22, y + 16), CYAN_BRIGHT),
        TerminalString(" seconds", Position(x + 26, y + 16), WHITE)
      )
        ++ ProgressBar.from(pb(40, skill.actionProgress, Position(x, y + 17), GREEN_BRIGHT))
    )

    headerStart.draw(graphics)
    asciiArt.draw(graphics)
    headerEnd.draw(graphics)
    xpBar.draw(graphics)
    actionBar.draw(graphics)
  end draw
end SkillDisplay

class InventoryDisplay:
  def draw(graphics: TextGraphics, state: GameState, position: Position): Unit =
    graphics.putString(position.x, 1, "Inventory")
    graphics.putString(position.x, 2, "---------")

    state.inventory.zipWithIndex.foreach { case ((item, count), index) =>
      graphics.putString(position.x, 3 + index, s"$item: $count")
    }
  end draw
end InventoryDisplay

class FpsDisplay(targetFps: Int):
  private var frameTime: Double                    = 0.0
  private val fpsUpdateIntervalMs: Milliseconds    = 10
  private var timeSinceLastFpsUpdate: Milliseconds = 0
  private var lastEndTime: Milliseconds            = 0

  def update(elapsedTime: Milliseconds): Unit =
    timeSinceLastFpsUpdate += elapsedTime

    if timeSinceLastFpsUpdate >= fpsUpdateIntervalMs then
      frameTime = elapsedTime
      timeSinceLastFpsUpdate = 0
    end if
  end update

  def draw(graphics: TextGraphics, position: Position): Unit =
    graphics.setForegroundColor(TextColor.ANSI.YELLOW)
    graphics.putString(position.x, position.y, f"FrameTime: $frameTime%.1f ms")
    graphics.putString(position.x, position.y + 1, f"FPS (real): ${1_000 / frameTime}%.1f")
    graphics.putString(position.x, position.y + 2, s"FPS (target): $targetFps")
    graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
  end draw
end FpsDisplay

class ScalaScape(forceTerminal: Boolean):
  private var running                = true
  private val state                  = new GameState
  private val menu                   = new Menu(List(Woodcutting(), Quarrying()), List(Woodworking(), Stonecutting()))
  private val inventoryDisplay       = new InventoryDisplay
  private val skillDisplay           = new SkillDisplay
  private val terminal: Terminal     = (new LanternBimbo).makeTerminal(forceTerminal)
  private val screen: Screen         = new TerminalScreen(terminal)
  private val graphics: TextGraphics = screen.newTextGraphics()
  private val targetFps              = 30
  private val fpsDisplay             = new FpsDisplay(targetFps)

  def run(): GameState =
    screen.startScreen()
    screen.clear()

    // Create an ExecutionContext for the game loop
    implicit val ec: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

    // Start the game loop
    Future {
      val targetFrameDuration: Milliseconds = (1_000 / targetFps).toLong
      while (running) {
        val startTime = System.nanoTime()

        update(state)
        render(graphics, state)
        screen.refresh()

        val endTime                       = System.nanoTime()
        val actualFrameTime: Milliseconds = (endTime - startTime) / 1_000_000

        // Update FPS counter
        fpsDisplay.update(actualFrameTime)

        // Enforce target frame rate by sleeping for the remaining time
        val sleepTime = targetFrameDuration - actualFrameTime
        if (sleepTime > 0) {
          Thread.sleep(sleepTime)
        }
      }
    }

    // Handle input for skill selection and activation
    Future {
      while (running) {
        val keyStroke: KeyStroke = screen.readInput()
        if keyStroke == KeyType.EOF then running = false
        else handleInput(keyStroke, state)
      }
    }

    state
  end run

  def update(state: GameState): GameState =
    state.activeSkill match {
      case Some(skill: Woodcutting) => skill.update(state, targetFps)
      case Some(skill: Quarrying)   => skill.update(state, targetFps)
      case _                        => // Do nothing
    }

    menu.update()

    state
  end update

  private def render(graphics: TextGraphics, state: GameState): GameState =
    screen.clear()

    menu.draw(graphics, state.activeSkill, Position(2, 1))
    skillDisplay.draw(graphics, state, Position(25, 1))
    inventoryDisplay.draw(graphics, state, Position(70, 1))
    fpsDisplay.draw(graphics, Position(100, 1))

    screen.setCursorPosition(null) // hide cursor
    screen.refresh()               // draw the diff to the screen

    state
  end render

  private def handleInput(keyStroke: KeyStroke, state: GameState): GameState =
    keyStroke.getKeyType match {
      case KeyType.ArrowDown                                  => menu.navigate(1)
      case KeyType.ArrowUp                                    => menu.navigate(-1)
      case KeyType.Enter                                      => menu.activateItem(state)
      case KeyType.Character if keyStroke.getCharacter == ' ' => menu.activateItem(state)
      case _                                                  => // Other keys can be handled here if necessary
    }

    state
  end handleInput
end ScalaScape
