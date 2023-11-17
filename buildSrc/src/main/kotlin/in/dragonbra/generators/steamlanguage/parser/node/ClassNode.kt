package `in`.dragonbra.generators.steamlanguage.parser.node

import `in`.dragonbra.generators.steamlanguage.parser.symbol.Symbol

data class ClassNode(
    var ident: Symbol? = null,
    var parent: Symbol? = null,
    var emit: Boolean = false
) : Node()
