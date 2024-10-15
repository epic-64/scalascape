package ScalaScape.components

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.TextColor.ANSI.GREEN_BRIGHT
import com.googlecode.lanterna.graphics.TextGraphics

case class Spinner(
    states: List[String] = "|/-\\".toList.map(_.toString),
    var index: Int = 0,
    var updateCounter: Int = 0
):
  def update(): Unit =
    if updateCounter >= 10 then
      index = (index + 1) % states.size
      updateCounter = 0
    else updateCounter += 1
  end update

  def currentState: String = states(index)
end Spinner

class Menu(val gatheringSkills: List[Skill], val manufacturingSkills: List[Skill]):
  private val items: List[String] =
    gatheringSkills.map(_.name)
      ++ manufacturingSkills.map(_.name)
      :+ "Inventory"

  private val spinner            = Spinner()
  private var selectedIndex: Int = 0

  def navigate(direction: Int): Unit =
    // Cycle through the menu items, including skills and inventory
    selectedIndex = selectedIndex + direction match {
      case i if i < 0           => items.size - 1
      case i if i >= items.size => 0
      case i                    => i
    }
  end navigate

  def activateItem(state: GameState): Unit =
    if (state.skills.contains(selectedItem)) {
      state.activeSkill = Some(state.skills(selectedItem))
    }
  end activateItem

  def selectedItem: String = items(selectedIndex)

  def update(): Unit =
    spinner.update()
  end update

  def render(
      graphics: TextGraphics,
      activeSkill: Option[Skill],
      position: Position
  ): Unit =
    val x = position.x

    def drawSkillItemText(skill: Skill, isActive: Boolean, isSelected: Boolean, position: Position): Unit = {
      val x            = position.x
      val y            = position.y
      val color        = if isActive then TextColor.ANSI.WHITE_BRIGHT else TextColor.ANSI.WHITE
      val spinnerState = if isActive then s"${spinner.currentState}" else ""

      graphics.setForegroundColor(color)
      graphics.putString(x, y, s"${if isSelected then ">" else " "} ${skill.name} $spinnerState")
      graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    }

    // Render gathering skill menu
    graphics.putString(x, 1, "Gathering")
    graphics.putString(x, 2, "---------")
    gatheringSkills.zipWithIndex.foreach { case (skill, index) =>
      drawSkillItemText(skill, activeSkill.contains(skill), selectedIndex == index, Position(x, 3 + index))
    }

    // Render manufacturing skill menu
    graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    graphics.putString(x, 4 + gatheringSkills.size, "Manufacturing")
    graphics.putString(x, 5 + gatheringSkills.size, "-------------")
    manufacturingSkills.zipWithIndex.foreach { case (skill, index) =>
      val isSelected = selectedIndex == gatheringSkills.size + index
      val position   = Position(x, 6 + gatheringSkills.size + index)
      drawSkillItemText(skill, activeSkill.contains(skill), isSelected, position)
    }

    // Render Management menu
    graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
    graphics.putString(x, 7 + gatheringSkills.size + manufacturingSkills.size, "Management")
    graphics.putString(x, 8 + gatheringSkills.size + manufacturingSkills.size, "----------")
    val inventoryIndex = gatheringSkills.size + manufacturingSkills.size
    val inventoryColor =
      if activeSkill.isEmpty && selectedItem == "Shop"
      then GREEN_BRIGHT
      else TextColor.ANSI.DEFAULT

    graphics.setForegroundColor(inventoryColor)
    val y = 9 + gatheringSkills.size + manufacturingSkills.size
    graphics.putString(x, y, s"${if (selectedIndex == inventoryIndex) ">" else " "} Shop")

    graphics.setForegroundColor(TextColor.ANSI.DEFAULT)
  end render
end Menu