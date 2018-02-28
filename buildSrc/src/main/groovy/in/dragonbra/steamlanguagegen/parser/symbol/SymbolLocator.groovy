package in.dragonbra.steamlanguagegen.parser.symbol

import java.util.regex.Pattern

import in.dragonbra.steamlanguagegen.parser.node.Node

class SymbolLocator {
    private static final Pattern IDENTIFIER_REGEX = ~/(?<identifier>-?[a-zA-Z0-9_:.]*)/
    private static final Pattern FULL_IDENTIFIER_REGEX = ~/(?<class>[a-zA-Z0-9_]*?)::(?<name>[a-zA-Z0-9_]*)/

    private static final WEAK_TYPES = [
            byte  : 'byte',
            short : 'Short',
            ushort: 'Integer',
            int   : 'Integer',
            uint  : 'Long',
            long  : 'Long',
            ulong : 'Long',
    ]

    private static Node findNode(Node tree, String symbol) {
        return tree.childNodes.stream().filter({ child -> child.name.equals(symbol) }).findFirst().orElse(null)
    }

    static Symbol lookupSymbol(Node tree, String identifier, boolean strongOnly) {
        def ident = IDENTIFIER_REGEX.matcher(identifier)

        if (!ident.matches()) {
            throw new IllegalArgumentException("Invalid identifier specified $identifier")
        }

        if (identifier.contains(".")) {
            def split = identifier.split("\\.")

            String val

            if (split[0].equals("ulong")) {
                switch (split[1]) {
                    case "MaxValue":
                        return new WeakSymbol("0xFFFFFFFFFFFFFFFF")
                    case "MinValue":
                        return new WeakSymbol("0x0000000000000000")
                    default:
                        return new WeakSymbol(identifier)
                }
            }

            switch (split[1]) {
                case "MaxValue":
                    val = "MAX_VALUE"
                    break
                case "MinValue":
                    val = "MIN_VALUE"
                    break
                default:
                    return new WeakSymbol(identifier)
            }

            return new WeakSymbol("${WEAK_TYPES[split[0]]}.$val")
        } else if (!identifier.contains("::")) {
            def classNode = findNode(tree, ident.group(0))

            if (classNode == null) {
                if (strongOnly) {
                    throw new IllegalStateException("Invalid weak symbol $identifier")
                } else {
                    return new WeakSymbol(identifier)
                }
            } else {
                return new StrongSymbol(classNode)
            }
        } else {
            ident = FULL_IDENTIFIER_REGEX.matcher(identifier)

            if (!ident.matches()) {
                throw new IllegalArgumentException("Couldn't parse full identifier $identifier")
            }

            def classNode = findNode(tree, ident.group("class"))

            if (classNode == null) {
                throw new IllegalStateException("Invalid class in identifier $identifier")
            }

            def propNode = findNode(classNode, ident.group("name"))

            if (propNode == null) {
                throw new IllegalStateException("Invalid property in identifier $identifier")
            }

            return new StrongSymbol(classNode, propNode)
        }
    }
}
