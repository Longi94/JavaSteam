package `in`.dragonbra.generators.steamlanguage.parser.node

import `in`.dragonbra.generators.steamlanguage.parser.symbol.Symbol

class PropNode(
    var flags: String? = null,
    var flagsOpt: String? = null,
    var type: Symbol? = null,
    var default: MutableList<Symbol> = mutableListOf(),
    var obsolete: String? = null,
    var emit: Boolean = true
) : Node()
