package ScalaScape.components

class GameState:
  val targetFps: Int              = 30
  var selectedScene: Scene        = WorldMenuScene()
  var inventory: Map[String, Int] = Map("Wood" -> 0, "Stone" -> 0)
  var activeSkill: Option[Skill]  = None
  var skills: Map[String, Skill]  = Map(
    "Woodcutting"  -> Woodcutting(),
    "Quarrying"    -> Quarrying(),
    "Woodworking"  -> Woodworking(),
    "Stonecutting" -> Stonecutting()
  )
end GameState
