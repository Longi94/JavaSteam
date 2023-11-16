package `in`.dragonbra.generators.steamlanguage.parser

import java.util.ArrayDeque
import java.util.Queue
import kotlin.text.Regex
import `in`.dragonbra.generators.steamlanguage.parser.token.TokenSourceInfo
import `in`.dragonbra.generators.steamlanguage.parser.token.Token

class LanguageParser {
    companion object {
        private val PATTERN: Regex = Regex(
            """(\s+)|(;)|"(.+?)"|//(.*)$|(-?[a-zA-Z_0-9][a-zA-Z0-9_:.]*)|(#[a-zA-Z]*)|([{}<>\]=|])|(\S+)""",
            RegexOption.MULTILINE
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
            val bufferLines = buffer.split("\\r?\\n".toRegex())
            val tokens = ArrayDeque<Token>()

            bufferLines.forEachIndexed { index, line ->
                val matcher = PATTERN.findAll(line)

                matcher.forEach { matchResult ->
                    var matchValue: String? = null
                    var groupName: String? = null

                    for (tempName in GROUP_NAMES) {
                        matchValue = matchResult.groups[tempName]?.value
                        groupName = tempName
                        if (matchValue != null) break
                    }

                    if (matchValue == null || groupName == "comment" || groupName == "whitespace") {
                        return@forEach
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
