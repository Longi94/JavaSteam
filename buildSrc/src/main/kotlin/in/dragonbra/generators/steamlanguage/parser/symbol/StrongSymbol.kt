package `in`.dragonbra.generators.steamlanguage.parser.symbol

import `in`.dragonbra.generators.steamlanguage.parser.node.Node

data class StrongSymbol(val clazz: Node, val prop: Node? = null) : Symbol
