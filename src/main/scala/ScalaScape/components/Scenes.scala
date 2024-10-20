package ScalaScape.components

import com.googlecode.lanterna.TextColor.ANSI.*
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

abstract class Scene(state: GameState):
  val name: String
  val description: String = "No description available"

  private def breadCrumbs: String =
    previousScene match
      case Some(scene) => scene.breadCrumbs + " / " + scene.name
      case None => ""

  private def renderHeader(pos: Pos): TerminalParagraph =
    def nameWithBreadcrumb = s"$breadCrumbs / $name"

    TerminalParagraph(List(TerminalString(description, pos, WHITE)))
      ++ TerminalParagraph(List(TerminalString("-" * 40, Pos(pos.x, pos.y + 1), WHITE)))
      ++ asciiArt(Pos(pos.x, pos.y + 2))
      ++ TerminalParagraph(List(TerminalString("-" * 40, Pos(pos.x, pos.y + 12), WHITE)))
      ++ TerminalParagraph(List(TerminalString(nameWithBreadcrumb, Pos(pos.x, pos.y + 13), WHITE)))
  end renderHeader

  def handleInput(key: KeyStroke, state: GameState): GameState =
    previousScene match
      case Some(scene) =>
        key.getKeyType match {
          case KeyType.Escape => state.swapScene(scene);
          case KeyType.ArrowLeft => state.swapScene(scene)
          case _ =>
        }
      case None => ()

    typeHandleInput(key, state)
  end handleInput

  def update(state: GameState): GameState =
    // add shared logic for all scenes here
    typeUpdate(state) // additional scene specific logic

  def render(state: GameState, pos: Pos): TerminalParagraph =
    renderHeader(pos) ++ typeRender(state, Pos(pos.x, pos.y + 14))
  end render

  def asciiArt(pos: Pos): TerminalParagraph = TerminalParagraph(List(TerminalString("No ASCII art available", pos)))

  def previousScene: Option[Scene]

  def typeHandleInput(key: KeyStroke, state: GameState): GameState = state
  def typeUpdate(state: GameState): GameState                      = state
  def typeRender(state: GameState, pos: Pos): TerminalParagraph
end Scene

abstract class MenuScene(state: GameState) extends Scene(state):
  lazy val menu: SceneMenu

  override def typeHandleInput(key: KeyStroke, state: GameState): GameState = menu.handleInput(key, state)
  override def typeRender(state: GameState, pos: Pos): TerminalParagraph = menu.render(Pos(pos.x, pos.y + 1))
end MenuScene

class WorldMenuScene(state: GameState) extends MenuScene(state):
  override val name = "W"
  override val description: String = "The world is your oyster."

  override def previousScene: Option[Scene] = None
  override def asciiArt(pos: Pos): TerminalParagraph = WorldMapArtwork(pos)

  override lazy val menu = SceneMenu(
    Map(
      "Gathering" -> GatheringMenuScene(state),
    )
  )
end WorldMenuScene

// Gathering
class GatheringMenuScene(state: GameState) extends MenuScene(state):
  override val name = "Gathering"

  override def previousScene = Some(WorldMenuScene(state))

  override lazy val menu = SceneMenu(
    Map(
      "Woodcutting" -> WoodCuttingMenuScene(state),
      "Go back" -> WorldMenuScene(state)
    )
  )
end GatheringMenuScene

class WoodCuttingMenuScene(state: GameState) extends MenuScene(state):
  override val name = "Woodcutting"
  override def previousScene = Some(GatheringMenuScene(state))

  val skill: Skill = state.skills.woodcutting

  def getLabel(mastery: Mastery): String =
    val level = mastery.level
    val requiredLevel = if skill.level < mastery.requiredParentLevel
    then s"Req Lvl ${mastery.requiredParentLevel}"
    else ""
    val name = mastery.name

    s"$name ($level / 99) $requiredLevel"

  val menuMap = Map(
    getLabel(skill.mastery[OakMastery]) -> OakScene(state),
    getLabel(skill.mastery[TeakMastery]) -> TeakScene(state),
    "Go back" -> GatheringMenuScene(state)
  )

  override lazy val menu = SceneMenu(menuMap)

  override def asciiArt(pos: Pos): TerminalParagraph = WoodCuttingArtwork(pos)
end WoodCuttingMenuScene

abstract class MasteryScene(state: GameState) extends Scene(state):
  def getMastery(state: GameState): Mastery

  override def typeUpdate(state: GameState): GameState =
    getMastery(state).update(state)

  override def typeRender(state: GameState, pos: Pos): TerminalParagraph =
    getMastery(state).render(Pos(pos.x, pos.y + 1), state)
end MasteryScene

class OakScene(state: GameState) extends MasteryScene(state):
  override val name        = "Oak"
  override val description = "Cut down some oak trees."

  override def getMastery(state: GameState): OakMastery = state.skills.woodcutting.mastery[OakMastery]
  override def asciiArt(pos: Pos): TerminalParagraph      = WoodCuttingArtwork(pos)

  override def previousScene = Some(WoodCuttingMenuScene(state))
end OakScene

class TeakScene(state: GameState) extends MasteryScene(state):
  override val name        = "Teak"
  override val description = "Cut down some teak trees."

  override def getMastery(state: GameState): TeakMastery = state.skills.woodcutting.mastery[TeakMastery]
  override def asciiArt(pos: Pos): TerminalParagraph       = WoodCuttingArtwork(pos)

  override def previousScene = Some(WoodCuttingMenuScene(state))
end TeakScene