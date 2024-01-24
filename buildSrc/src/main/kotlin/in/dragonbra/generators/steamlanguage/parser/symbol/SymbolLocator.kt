package `in`.dragonbra.generators.steamlanguage.parser.symbol

import `in`.dragonbra.generators.steamlanguage.parser.node.Node

class SymbolLocator {
    companion object {
        private val IDENTIFIER_REGEX = "(?<identifier>-?[a-zA-Z0-9_:.]*)".toRegex()
        private val FULL_IDENTIFIER_REGEX = "(?<class>[a-zA-Z0-9_]*?)::(?<name>[a-zA-Z0-9_]*)".toRegex()

        private val WEAK_TYPES: Map<String, String> = mapOf(
            "byte" to "byte",
            "short" to "Short",
            "ushort" to "Integer",
            "int" to "Integer",
            "uint" to "Long",
            "long" to "Long",
            "ulong" to "Long"
        )

        private fun findNode(tree: Node, symbol: String?): Node? =
            tree.childNodes.firstOrNull { child -> child.name == symbol }

        fun lookupSymbol(tree: Node, identifier: String, strongOnly: Boolean): Symbol {
            var ident: MatchResult? = IDENTIFIER_REGEX.matchEntire(identifier)
                ?: throw IllegalArgumentException("Invalid identifier specified $identifier")

            if (identifier.contains(".")) {
                val split = identifier.split(".")

                if (split[0] == "ulong") {
                    when (split[1]) {
                        "MaxValue" -> WeakSymbol("0xFFFFFFFFFFFFFFFF")
                        "MinValue" -> WeakSymbol("0x0000000000000000")
                        else -> WeakSymbol(identifier)
                    }
                }

                val value: String = when (split[1]) {
                    "MaxValue" -> "MAX_VALUE"
                    "MinValue" -> "MIN_VALUE"
                    else -> return WeakSymbol(identifier)
                }

                return WeakSymbol("${WEAK_TYPES[split[0]]}.$value")
            } else if (!identifier.contains("::")) {
                val classNode = findNode(tree, ident?.groupValues?.get(0))

                return if (classNode == null) {
                    if (strongOnly) {
                        throw IllegalStateException("Invalid weak symbol $identifier")
                    }

                    WeakSymbol(identifier)
                } else {
                    StrongSymbol(classNode)
                }
            } else {
                ident = FULL_IDENTIFIER_REGEX.matchEntire(identifier)

                if (ident == null) {
                    throw IllegalArgumentException("Couldn't parse full identifier $identifier")
                }

                val classNode = findNode(tree, ident.groupValues[1])
                    ?: throw IllegalStateException("Invalid class in identifier $identifier")

                val propNode = findNode(classNode, ident.groupValues[2])
                    ?: throw IllegalStateException("Invalid property in identifier $identifier")

                return StrongSymbol(classNode, propNode)
            }
        }
    }
}
