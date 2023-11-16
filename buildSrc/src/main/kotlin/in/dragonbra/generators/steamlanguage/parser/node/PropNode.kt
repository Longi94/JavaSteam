package `in`.dragonbra.generators.steamlanguage.parser.node

import `in`.dragonbra.generators.steamlanguage.parser.symbol.Symbol

class PropNode(
    val flags: String,
    val flagsOpt: String? = null,
    val type: Symbol,
    val default: List<Symbol> = listOf(),
    val obsolete: String? = null,
    val emit: Boolean = true
) : Node()
