package in.dragonbra.steamlanguageparser.parser;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Queue;

/**
 * @author lngtr
 * @since 2018-02-15
 */
public class TokenAnalyzer {

    public static Node analyze(Queue<Token> tokens) throws IOException {
        Node root = new Node();

        while (!tokens.isEmpty()) {
            Token cur = tokens.poll();

            switch (cur.getName()) {
                case "EOF":
                    break;
                case "preprocess":
                    Token text = expect(tokens, "string");

                    if ("import".equals(cur.getValue())) {
                        Queue<Token> parentTokens = LanguageParser
                                .tokenizeString(IOUtils.toString(new FileInputStream(new File(text.getValue())), "utf-8"));

                        Node newRoot = analyze(parentTokens);

                        newRoot.getChildNodes().forEach(child -> root.getChildNodes().add(child));
                    }
                    break;
                case "identifier":
                    Token name, op1;
                    switch (cur.getValue()) {
                        case "class":
                            name = expect(tokens, "identifier");
                            Token ident = null;
                            Token parent = null;

                            op1 = optional(tokens, "operator", "<");
                            if (op1 != null) {
                                ident = expect(tokens, "identifier");
                                expect(tokens, "operator", ">");
                            }

                            Token expect = optional(tokens, "identifier", "expects");
                            if (expect != null) {
                                parent = expect(tokens, "identifier");
                            }

                            Token removed = optional(tokens, "identifier", "removed");
                            if (removed != null) {
                                optional(tokens, "string");
                                optional(tokens, "terminator");
                            }

                            ClassNode classNode = new ClassNode();
                            classNode.setName(name.getValue());

                            if (ident != null) {
                                classNode.setIdent(SymbolLocator.lookupSymbol(root, ident.getValue(), false));
                            }

                            if (parent != null) {
                                classNode.setParent(SymbolLocator.lookupSymbol(root, parent.getValue(), true));
                            }

                            classNode.setEmit(removed == null);

                            root.getChildNodes().add(classNode);
                            parseInnerScope(tokens, classNode, root);
                            break;
                        case "enum":
                            name = expect(tokens, "identifier");
                            Token datatype = null;

                            op1 = optional(tokens, "operator", "<");
                            if (op1 != null) {
                                datatype = expect(tokens, "identifier");
                                expect(tokens, "operator", ">");
                            }

                            Token flag = optional(tokens, "identifier", "flags");

                            EnumNode enode = new EnumNode();
                            enode.setName(name.getValue());

                            if (flag != null) {
                                enode.setFlags(flag.getValue());
                                ;
                            }

                            if (datatype != null) {
                                enode.setType(SymbolLocator.lookupSymbol(root, datatype.getValue(), false));
                            }


                            root.getChildNodes().add(enode);
                            parseInnerScope(tokens, enode, root);
                            break;
                    }
            }
        }

        return root;
    }

    private static void parseInnerScope(Queue<Token> tokens, Node parent, Node root) {
        expect(tokens, "operator", "{");
        Token scope2 = optional(tokens, "operator", "}");

        while (scope2 == null) {
            PropNode pnode = new PropNode();

            Token t1 = tokens.poll();

            Token t1op1 = optional(tokens, "operator", "<");
            Token flagop = null;

            if (t1op1 != null) {
                flagop = expect(tokens, "identifier");
                expect(tokens, "operator", ">");

                pnode.setFlagsOpt(flagop.getValue());
            }

            Token t2 = optional(tokens, "identifier");
            Token t3 = optional(tokens, "identifier");

            if (t3 != null) {
                pnode.setName(t3.getValue());
                pnode.setType(SymbolLocator.lookupSymbol(root, t2.getValue(), false));
                pnode.setFlags(t1.getValue());
            } else if (t2 != null) {
                pnode.setName(t2.getValue());
                pnode.setType(SymbolLocator.lookupSymbol(root, t1.getValue(), false));
            } else {
                pnode.setName(t1.getValue());
            }

            Token defop = optional(tokens, "operator", "=");

            if (defop != null) {
                while (true) {
                    Token value = tokens.poll();
                    pnode.getDefault().add(SymbolLocator.lookupSymbol(root, value.getValue(), false));

                    if (optional(tokens, "operator", "|") != null)
                        continue;

                    expect(tokens, "terminator", ";");
                    break;
                }
            } else {
                expect(tokens, "terminator", ";");
            }

            Token obsolete = optional(tokens, "identifier", "obsolete");
            if (obsolete != null) {
                pnode.setObsolete("");

                Token obsoleteReason = optional(tokens, "string");

                if (obsoleteReason != null)
                    pnode.setObsolete(obsoleteReason.getValue());
            }

            Token removed = optional(tokens, "identifier", "removed");
            if (removed != null) {
                pnode.setEmit(false);
                optional(tokens, "string");
                optional(tokens, "terminator");
            }

            parent.getChildNodes().add(pnode);

            scope2 = optional(tokens, "operator", "}");
        }
    }

    private static Token expect(Queue<Token> tokens, String name) {
        Token peek = tokens.peek();

        if (peek == null) {
            return new Token("EOF", "");
        }

        if (!peek.getName().equals(name)) {
            throw new IllegalStateException("Expecting " + name);
        }

        return tokens.poll();
    }

    private static Token expect(Queue<Token> tokens, String name, String value) {
        Token peek = tokens.peek();

        if (peek == null) {
            return new Token("EOF", "");
        }

        if (!peek.getName().equals(name) || !peek.getValue().equals(value)) {
            if (peek.getSource() != null) {
                TokenSourceInfo source = peek.getSource();
                throw new IllegalStateException(String.format("Expecting {%s} '{%s}', but got '%s' at %s %d,%d-%d,%d",
                        name, value, peek.getValue(), source.getFileName(), source.getStartLineNumber(),
                        source.getStartColumnNumber(), source.getEndLineNumber(), source.getEndColumnNumber()));
            } else {
                throw new IllegalStateException("Expecting " + name + " '" + value + "', but got '" + peek.getValue() + "'");
            }
        }

        return tokens.poll();
    }

    private static Token optional(Queue<Token> tokens, String name) {
        Token peek = tokens.peek();

        if (peek == null) {
            return new Token("EOF", "");
        }

        if (!peek.getName().equals(name)) {
            return null;
        }

        return tokens.poll();
    }

    private static Token optional(Queue<Token> tokens, String name, String value) {
        Token peek = tokens.peek();

        if (peek == null) {
            return new Token("EOF", "");
        }

        if (!peek.getName().equals(name) || !peek.getValue().equals(value)) {
            return null;
        }

        return tokens.poll();
    }
}
