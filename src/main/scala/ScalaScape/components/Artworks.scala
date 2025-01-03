package ScalaScape.components

import ScalaScape.utils.TerminalArt
import com.googlecode.lanterna.TextColor.ANSI.*

def WoodCuttingArtwork(pos: Pos) =
  val art: String =
    """|               ,@@@@@@@,
       |       ,,,.   ,@@@@@@/@@,  .oo8888o.
       |    ,&%%&%&&%,@@@@@/@@@@@@,:8888\88/8o
       |   ,%&\%&&%&&%,@@@\@@/@@@88\88888/88'
       |   %&&%&%&/%&&%@@\@@/ /@@@88888\88888'
       |   %&&%/ %&%%&&@@\ V /@@' `88\8 `/88'
       |   `&%\ ` /%&'    |.|        \ '|8'
       |       |o|        | |         | |
       |       |.|        | |         | |
       | ___ \/ ._\//_/__/  ,\_\//__\/.  \_//__
       |""".stripMargin

  val colors: String =
    """|               ,@@@@@@@,
       |       ,,,.   ,@@@@@@/@@,  .oo8888o.
       |    ,&%%&%&&%,@@@@@/@@@@@@,:8888\88/8o
       |   ,%&\%&&%&&%,@@@\@@/@@@88\88888/88'
       |   %&&%&%&/%&&%@@\@@/ /@@@88888\88888'
       |   %&&%/ %&%%&&@@\ V /@@' `88\8 `/88'
       |   `&%\ ` /%&'    |.|        \ '|8'
       |       |W|        | |         | |
       |       |.|        | |         | |
       | ___ B/ ._\BG_B__/  G\_BGG__B/.  \_BG__
       |""".stripMargin

  val colorMap = Map(
    '@' -> GREEN_BRIGHT,
    '&' -> GREEN,
    '%' -> GREEN,
    '8' -> GREEN,
    'o' -> GREEN,
    'G' -> GREEN,
    'B' -> GREEN_BRIGHT,
    'W' -> WHITE
  )

  RenderedBlock(TerminalArt.parse(art, colors, pos, colorMap))
end WoodCuttingArtwork

def WorldMapArtwork(pos: Pos) =
  val art: String =
    """|           _T      .,,.    ~--~ ^^
     |     ^^   // \                    ~
     |          ][O]    ^^      ,-~ ~
     |       /''-I_I         _II____
     |    __/_  /   \ ______/ ''   /'\_,__
     |      | II--'''' \,--:--..,_/,.-{ },
     |    ; '/__\,.--';|   |[] .-.| O{ _ }
     |    :' |  | []  -|   ''--:.;[,.'\,/
     |    '  |[]|,.--'' '',   ''-,.    |
     |      ..    ..-''    ;       ''. '
     """.stripMargin

  val colors: String =
    """|           _T      .,,.    ~--~ ^^
       |     ^^   // \                    ~
       |          ][O]    ^^      ,-~ ~
       |       /''-I_I         _II____
       |    __/_  /   \ ______/ ''   /'\_,__
       |      | II--'''' \,--:--..,_/,.-{ },
       |    ; '/__\,.--';|   |BB .-.| O{ _ }
       |    :' |  | BB  -|   ''--:.;[,.'\,/
       |    '  |[]|,.--'' '',   ''-,.    |
       |      ..    ..-''    ;       ''. '
       """.stripMargin

  val colorMap = Map(
    '\'' -> YELLOW,
    '^'  -> WHITE_BRIGHT,
    'C'  -> CYAN,
    'B'  -> BLUE_BRIGHT
  )

  RenderedBlock(TerminalArt.parse(art, colors, pos, colorMap))
end WorldMapArtwork
