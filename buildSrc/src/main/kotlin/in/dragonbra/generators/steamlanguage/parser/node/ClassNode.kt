package `in`.dragonbra.generators.steamlanguage.parser.node

import `in`.dragonbra.generators.steamlanguage.parser.symbol.Symbol

data class ClassNode(
    val ident: Symbol? = null,
    val parent: Symbol? = null,
    val emit: Boolean,
) : Node()
