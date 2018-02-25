package in.dragonbra.steamlanguageparser.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lngtr
 * @since 2018-02-15
 */
public class SymbolLocator {
    private static final Pattern IDENTIFIER_REGEX = Pattern.compile("(?<identifier>-?[a-zA-Z0-9_:.]*)");
    private static final Pattern FULL_IDENTIFIER_REGEX = Pattern.compile("(?<class>[a-zA-Z0-9_]*?)::(?<name>[a-zA-Z0-9_]*)");

    private static final Map<String, String> WEAK_TYPES;

    static {
        Map<String, String> weakTypes = new HashMap<>();

        weakTypes.put("byte", "Byte");
        weakTypes.put("short", "Short");
        weakTypes.put("ushort", "Integer");
        weakTypes.put("int", "Integer");
        weakTypes.put("uint", "Long");
        weakTypes.put("long", "Long");
        weakTypes.put("ulong", "Long");

        WEAK_TYPES = Collections.unmodifiableMap(weakTypes);
    }

    private static Node findNode(Node tree, String symbol) {
        return tree.getChildNodes().stream().filter(child -> child.getName().equals(symbol)).findFirst().orElse(null);
    }

    public static Symbol lookupSymbol(Node tree, String identifier, boolean strongOnly) {
        Matcher ident = IDENTIFIER_REGEX.matcher(identifier);

        if (!ident.matches()) {
            throw new IllegalArgumentException("Invalid identifier specified " + identifier);
        }

        if (identifier.contains(".")) {
            String[] split = identifier.split("\\.");

            String val;

            if (split[0].equals("ulong")) {
                switch (split[1]) {
                    case "MaxValue":
                        return new WeakSymbol("0xFFFFFFFFFFFFFFFF");
                    case "MinValue":
                        return new WeakSymbol("0x0000000000000000");
                    default:
                        return new WeakSymbol(identifier);
                }
            }

            switch (split[1]) {
                case "MaxValue":
                    val = "MAX_VALUE";
                    break;
                case "MinValue":
                    val = "MIN_VALUE";
                    break;
                default:
                    return new WeakSymbol(identifier);
            }

            return new WeakSymbol(WEAK_TYPES.get(split[0]) + "." + val);
        } else if (!identifier.contains("::")) {
            Node classNode = findNode(tree, ident.group(0));

            if (classNode == null) {
                if (strongOnly) {
                    throw new IllegalStateException("Invalid weak symbol " + identifier);
                } else {
                    return new WeakSymbol(identifier);
                }
            } else {
                return new StrongSymbol(classNode);
            }
        } else {
            ident = FULL_IDENTIFIER_REGEX.matcher(identifier);

            if (!ident.matches()) {
                throw new IllegalArgumentException("Couldn't parse full identifier " + identifier);
            }

            Node classNode = findNode(tree, ident.group("class"));

            if (classNode == null) {
                throw new IllegalStateException("Invalid class in identifier " + identifier);
            }

            Node propNode = findNode(classNode, ident.group("name"));

            if (propNode == null) {
                throw new IllegalStateException("Invalid property in identifier " + identifier);
            }

            return new StrongSymbol(classNode, propNode);
        }
    }
}
