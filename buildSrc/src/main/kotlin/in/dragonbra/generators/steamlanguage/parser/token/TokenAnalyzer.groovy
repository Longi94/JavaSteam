package in.dragonbra.generators.steamlanguage.parser.token


import org.apache.commons.io.IOUtils

class TokenAnalyzer {
    static in.dragonbra.generators.steamlanguage.parser.node.Node analyze(Queue<Token> tokens, String dir) throws IOException {
        def root = new in.dragonbra.generators.steamlanguage.parser.node.Node()

        while (!tokens.isEmpty()) {
            def cur = tokens.poll()

            switch (cur.name) {
                case 'EOF':
                    break
                case 'preprocess':
                    def text = expect(tokens, 'string')

                    if ('import' == cur.value) {
                        Queue<Token> parentTokens = in.dragonbra.generators.steamlanguage.parser.LanguageParser
                                .tokenizeString(IOUtils.toString(new FileInputStream(new File("$dir/$text.value")), 'utf-8'), text.value)

                        in.dragonbra.generators.steamlanguage.parser.node.Node newRoot = analyze(parentTokens, dir)

                        newRoot.childNodes.forEach({ child -> root.childNodes << child })
                    }
                    break
                case 'identifier':
                    Token name, op1
                    switch (cur.value) {
                        case 'class':
                            name = expect(tokens, 'identifier')
                            Token ident = null
                            Token parent = null

                            op1 = optional(tokens, 'operator', '<')
                            if (op1 != null) {
                                ident = expect(tokens, 'identifier')
                                expect(tokens, 'operator', '>')
                            }

                            def expectToken = optional(tokens, 'identifier', 'expects')
                            if (expectToken != null) {
                                parent = expect(tokens, 'identifier')
                            }

                            def removed = optional(tokens, 'identifier', 'removed')
                            if (removed != null) {
                                optional(tokens, 'string')
                                optional(tokens, 'terminator')
                            }

                            def classNode = new in.dragonbra.generators.steamlanguage.parser.node.ClassNode()
                            classNode.name = name.value

                            if (ident != null) {
                                classNode.ident = in.dragonbra.generators.steamlanguage.parser.symbol.SymbolLocator.lookupSymbol(root, ident.value, false)
                            }

                            if (parent != null) {
                                classNode.parent = in.dragonbra.generators.steamlanguage.parser.symbol.SymbolLocator.lookupSymbol(root, parent.value, true)
                            }

                            classNode.emit = removed == null

                            root.childNodes << classNode
                            parseInnerScope(tokens, classNode, root)
                            break
                        case 'enum':
                            name = expect(tokens, 'identifier')
                            Token datatype = null

                            op1 = optional(tokens, 'operator', '<')
                            if (op1 != null) {
                                datatype = expect(tokens, 'identifier')
                                expect(tokens, 'operator', '>')
                            }

                            def flag = optional(tokens, 'identifier', 'flags')

                            def enode = new in.dragonbra.generators.steamlanguage.parser.node.EnumNode()
                            enode.name = name.value

                            if (flag != null) {
                                enode.flags = flag.value
                            }

                            if (datatype != null) {
                                enode.type = in.dragonbra.generators.steamlanguage.parser.symbol.SymbolLocator.lookupSymbol(root, datatype.value, false)
                            }

                            root.childNodes << enode
                            parseInnerScope(tokens, enode, root)
                            break
                    }
            }
        }

        return root
    }

    @SuppressWarnings('GroovyUnusedAssignment')
    private static void parseInnerScope(Queue<Token> tokens, in.dragonbra.generators.steamlanguage.parser.node.Node parent, in.dragonbra.generators.steamlanguage.parser.node.Node root) {
        expect(tokens, 'operator', '{')
        def scope2 = optional(tokens, 'operator', '}')

        while (scope2 == null) {
            def pnode = new in.dragonbra.generators.steamlanguage.parser.node.PropNode()

            def t1 = tokens.poll()

            def t1op1 = optional(tokens, 'operator', '<')
            Token flagop = null

            if (t1op1 != null) {
                flagop = expect(tokens, 'identifier')
                expect(tokens, 'operator', '>')

                pnode.flagsOpt = flagop.value
            }

            def t2 = optional(tokens, 'identifier')
            def t3 = optional(tokens, 'identifier')

            if (t3 != null) {
                pnode.name = t3.value
                pnode.type = in.dragonbra.generators.steamlanguage.parser.symbol.SymbolLocator.lookupSymbol(root, t2.value, false)
                pnode.flags = t1.value
            } else if (t2 != null) {
                pnode.name = t2.value
                pnode.type = in.dragonbra.generators.steamlanguage.parser.symbol.SymbolLocator.lookupSymbol(root, t1.value, false)
            } else {
                pnode.name = t1.value
            }

            def defop = optional(tokens, 'operator', '=')

            if (defop != null) {
                while (true) {
                    def value = tokens.poll()
                    pnode._default << in.dragonbra.generators.steamlanguage.parser.symbol.SymbolLocator.lookupSymbol(root, value.value, false)

                    if (optional(tokens, 'operator', '|') != null)
                        continue

                    expect(tokens, 'terminator', ';')
                    break
                }
            } else {
                expect(tokens, 'terminator', ';')
            }

            def obsolete = optional(tokens, 'identifier', 'obsolete')
            if (obsolete != null) {
                pnode.obsolete = ''

                def obsoleteReason = optional(tokens, 'string')

                if (obsoleteReason != null)
                    pnode.obsolete = obsoleteReason.value
            }

            def removed = optional(tokens, 'identifier', 'removed')
            if (removed != null) {
                pnode.emit = false
                optional(tokens, 'string')
                optional(tokens, 'terminator')
            }

            parent.childNodes << pnode

            scope2 = optional(tokens, 'operator', '}')
        }
    }

    private static Token expect(Queue<Token> tokens, String name) {
        def peek = tokens.peek()

        if (peek == null) {
            return new Token('EOF', '')
        }

        if (peek.name != name) {
            throw new IllegalStateException("Expecting $name")
        }

        return tokens.poll()
    }

    private static Token expect(Queue<Token> tokens, String name, String value) {
        def peek = tokens.peek()

        if (peek == null) {
            return new Token('EOF', '')
        }

        if (peek.name != name || peek.value != value) {
            if (peek.source != null) {
                def source = peek.source
                throw new IllegalStateException("Expecting {$name} '{$value}', but got '$peek.value' at $source.fileName $source.startLineNumber, $source.startColumnNumber-$source.endLineNumber, $source.endColumnNumber")
            } else {
                throw new IllegalStateException("Expecting $name '$value', but got '$peek.value'")
            }
        }

        return tokens.poll()
    }

    private static Token optional(Queue<Token> tokens, String name) {
        def peek = tokens.peek()

        if (peek == null) {
            return new Token('EOF', '')
        }

        if (peek.name != name) {
            return null
        }

        return tokens.poll()
    }

    private static Token optional(Queue<Token> tokens, String name, String value) {
        def peek = tokens.peek()

        if (peek == null) {
            return new Token('EOF', '')
        }

        if (peek.name != name || peek.value != value) {
            return null
        }

        return tokens.poll()
    }
}
