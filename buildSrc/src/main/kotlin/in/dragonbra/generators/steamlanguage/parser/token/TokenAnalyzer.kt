package `in`.dragonbra.generators.steamlanguage.parser.token

import `in`.dragonbra.generators.steamlanguage.parser.LanguageParser
import `in`.dragonbra.generators.steamlanguage.parser.node.ClassNode
import `in`.dragonbra.generators.steamlanguage.parser.node.EnumNode
import org.apache.commons.io.IOUtils
import `in`.dragonbra.generators.steamlanguage.parser.node.Node
import `in`.dragonbra.generators.steamlanguage.parser.node.PropNode
import `in`.dragonbra.generators.steamlanguage.parser.symbol.SymbolLocator
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.*

class TokenAnalyzer {
    companion object {
        @Throws(IOException::class)
        fun analyze(tokens: Queue<Token>, dir: String): Node {
            val root = Node()

            while (!tokens.isEmpty()) {
                val cur = tokens.poll()

                when (cur.name) {
                    "EOF" -> Unit
                    "preprocess" -> {
                        val text = expect(tokens, "string")

                        if ("import" == cur.value) {
                            val parentTokens = LanguageParser.tokenizeString(IOUtils.toString(FileInputStream(File("$dir/${text.value}")), "utf-8"), text.value)

                            val newRoot = analyze(parentTokens, dir)

                            newRoot.childNodes.forEach { child -> root.childNodes.add(child) }
                        }

                    }

                    "identifier" -> {
                        var name: Token?
                        var op1: Token?

                        when (cur.value) {
                            "class" -> {
                                name = expect(tokens, "identifier")

                                var ident: Token? = null
                                var parent: Token? = null

                                op1 = optional(tokens, "operator", "<")
                                if (op1 != null) {
                                    ident = expect(tokens, "identifier")
                                    expect(tokens, "operator", ">")
                                }

                                val expectToken = optional(tokens, "identifier", "expects")
                                if (expectToken != null) {
                                    parent = expect(tokens, "identifier")
                                }

                                val removed = optional(tokens, "identifier", "removed")

                                if (removed != null) {
                                    optional(tokens, "string")
                                    optional(tokens, "terminator")
                                }

                                val classNode = ClassNode()

                                classNode.name = name.value

                                if (ident != null) {
                                    classNode.ident = SymbolLocator.lookupSymbol(root, ident.value, false)
                                }

                                if (parent != null) {
                                    classNode.parent = SymbolLocator.lookupSymbol(root, parent.value, true)
                                }

                                classNode.emit = removed == null

                                root.childNodes.add(classNode)

                                parseInnerScope(tokens, classNode, root)
                            }

                            "enum" -> {
                                name = expect(tokens, "identifier")
                                var datatype: Token? = null

                                op1 = optional(tokens, "operator", "<")
                                if (op1 != null) {
                                    datatype = expect(tokens, "identifier")
                                    expect(tokens, "operator", ">")
                                }

                                val flag = optional(tokens, "identifier", "flags")

                                val enode = EnumNode()
                                enode.name = name.value

                                if (flag != null) {
                                    enode.flags = flag.value
                                }

                                if (datatype != null) {
                                    enode.type = SymbolLocator.lookupSymbol(root, datatype.value, false)
                                }

                                root.childNodes.add(enode)
                                parseInnerScope(tokens, enode, root)
                            }
                        }
                    }
                }
            }

            return root
        }

        private fun parseInnerScope(tokens: Queue<Token>, parent: Node, root: Node) {
            expect(tokens, "operator", "{")
            var scope2 = optional(tokens, "operator", "}")

            while (scope2 == null) {
                val pnode = PropNode()

                val t1 = tokens.poll()

                val t1op1 = optional(tokens, "operator", "<")
                var flagop: Token?

                if (t1op1 != null) {
                    flagop = expect(tokens, "identifier")
                    expect(tokens, "operator", ">")

                    pnode.flagsOpt = flagop.value
                }

                val t2 = optional(tokens, "identifier")
                val t3 = optional(tokens, "identifier")

                if (t3 != null) {
                    pnode.name = t3.value
                    pnode.type = SymbolLocator.lookupSymbol(root, t2!!.value, false)
                    pnode.flags = t1.value
                } else if (t2 != null) {
                    pnode.name = t2.value
                    pnode.type = SymbolLocator.lookupSymbol(root, t1.value, false)
                } else {
                    pnode.name = t1.value
                }

                val defop = optional(tokens, "operator", "=")

                if (defop != null) {
                    while (true) {
                        val value = tokens.poll()
                        pnode.default.add(SymbolLocator.lookupSymbol(root, value.value, false))

                        if (optional(tokens, "operator", "|") != null)
                            continue

                        expect(tokens, "terminator", ";")
                        break
                    }
                } else {
                    expect(tokens, "terminator", ";")
                }

                val obsolete = optional(tokens, "identifier", "obsolete")
                if (obsolete != null) {
                    pnode.obsolete = ""

                    val obsoleteReason = optional(tokens, "string")

                    if (obsoleteReason != null)
                        pnode.obsolete = obsoleteReason.value
                }

                val removed = optional(tokens, "identifier", "removed")
                if (removed != null) {
                    pnode.emit = false
                    optional(tokens, "string")
                    optional(tokens, "terminator")
                }

                parent.childNodes.add(pnode)

                scope2 = optional(tokens, "operator", "}")
            }
        }

        private fun expect(tokens: Queue<Token>, name: String): Token {
            val peek = tokens.peek() ?: return Token("EOF", "")

            if (peek.name != name) {
                throw IllegalStateException("Expecting $name")
            }

            return tokens.poll()
        }

        private fun expect(tokens: Queue<Token>, name: String, value: String): Token {
            val peek = tokens.peek() ?: return Token("EOF", "")

            if (peek.name != name || peek.value != value) {
                if (peek.source != null) {
                    val source = peek.source
                    throw IllegalStateException("Expecting {$name} '{$value}', but got '${peek.value}' at ${source.fileName} ${source.startLineNumber}, ${source.startColumnNumber}-${source.endLineNumber}, ${source.endColumnNumber}")
                } else {
                    throw IllegalStateException("Expecting $name '$value', but got '${peek.value}'")
                }
            }

            return tokens.poll()
        }

        private fun optional(tokens: Queue<Token>, name: String): Token? {
            val peek = tokens.peek() ?: return Token("EOF", "")

            if (peek.name != name) {
                return null
            }

            return tokens.poll()
        }

        private fun optional(tokens: Queue<Token>, name: String, value: String): Token? {
            val peek = tokens.peek() ?: return Token("EOF", "")

            if (peek.name != name || peek.value != value) {
                return null
            }

            return tokens.poll()
        }
    }
}