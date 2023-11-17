package `in`.dragonbra.generators.steamlanguage.parser

import java.util.ArrayDeque
import java.util.Queue
import `in`.dragonbra.generators.steamlanguage.parser.token.TokenSourceInfo
import `in`.dragonbra.generators.steamlanguage.parser.token.Token
import java.util.regex.Pattern

class LanguageParser {
    companion object {
        private val PATTERN: Pattern = Pattern.compile(
            """(?<whitespace>\s+)|""" +
                """(?<terminator>[;])|""" +
                """["](?<string>.+?)["]|""" +
                """//(?<comment>.*)$|""" +
                """(?<identifier>-?[a-zA-Z_0-9][a-zA-Z0-9_:.]*)|""" +
                """[#](?<preprocess>[a-zA-Z]*)|""" +
                """(?<operator>[{}<>\]=|])|""" +
                """(?<invalid>[^\s]+)""",
            Pattern.MULTILINE
        )

        private val GROUP_NAMES = listOf(
            "whitespace",
            "terminator",
            "string",
            "comment",
            "identifier",
            "preprocess",
            "operator",
            "invalid"
        )

        fun tokenizeString(buffer: String, fileName: String): Queue<Token> {
            val bufferLines = buffer.split("[\\r\\n]+")
            val tokens = ArrayDeque<Token>()

            bufferLines.forEachIndexed { index, line ->
                val matcher = PATTERN.matcher(line)

                while (matcher.find()) {
                    var matchValue: String? = null
                    var groupName: String? = null

                    for (tempName in GROUP_NAMES) {
                        matchValue = matcher.group(tempName)
                        groupName = tempName
                        if (matchValue != null) {
                            break
                        }
                    }

                    if (matchValue == null || groupName == "comment" || groupName == "whitespace") {
                        continue
                    }

                    val startColumnNumber = line.indexOf(matchValue)
                    val endColumnNumber = startColumnNumber + matchValue.length
                    val source = TokenSourceInfo(fileName, index, startColumnNumber, index, endColumnNumber)
                    val token = Token(groupName, matchValue, source)

                    tokens.add(token)
                }
            }

            return tokens
        }
    }
}
