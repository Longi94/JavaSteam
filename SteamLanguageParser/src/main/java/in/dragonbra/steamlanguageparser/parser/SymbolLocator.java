package in.dragonbra.steamlanguageparser.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lngtr
 * @since 2018-02-15
 */
public class SymbolLocator {
    private static final Pattern IDENTIFIER_REGEX = Pattern.compile("(?<identifier>-?[a-zA-Z0-9_:]*)");
    private static final Pattern FULL_IDENTIFIER_REGEX = Pattern.compile("(?<class>[a-zA-Z0-9_]*?)::(?<name>[a-zA-Z0-9_]*)");

    private static Node findNode(Node tree, String symbol) {
        return tree.getChildNodes().stream().filter(child -> child.getName().equals(symbol)).findFirst().orElse(null);
    }

    public static Symbol lookupSymbol(Node tree, String identifier, boolean strongOnly) {
        Matcher ident = IDENTIFIER_REGEX.matcher(identifier);

        if (!ident.matches()) {
            throw new IllegalArgumentException("Invalid identifier specified " + identifier);
        }

        if (!identifier.contains("::")) {
            Node classNode = findNode(tree, ident.group(0));

            if (classNode == null) {
                if (strongOnly) {
                    throw new IllegalStateException("Invalid weak symbol " + identifier);
                } else {
                    return new WeakSymbol(identifier);
                }
            } else  {
                return new StrongSymbol(classNode);
            }
        } else {
            ident = FULL_IDENTIFIER_REGEX.matcher(identifier);

            if (!ident.matches()) {
                throw new IllegalArgumentException("Couldn't parse full identifier " + identifier);
            }

            Node classNode = findNode(tree, ident.group("class"));

            if (classNode == null)
            {
                throw new IllegalStateException("Invalid class in identifier " + identifier);
            }

            Node propNode = findNode(classNode, ident.group("name"));

            if (propNode == null)
            {
                throw new IllegalStateException("Invalid property in identifier " + identifier);
            }

            return new StrongSymbol(classNode, propNode);
        }
    }
}
