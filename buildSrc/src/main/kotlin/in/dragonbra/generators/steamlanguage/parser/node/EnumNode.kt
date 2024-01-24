package `in`.dragonbra.generators.steamlanguage.parser.node

import `in`.dragonbra.generators.steamlanguage.parser.symbol.Symbol

class EnumNode(
    var flags: String? = null,
    var type: Symbol? = null
) : Node()
