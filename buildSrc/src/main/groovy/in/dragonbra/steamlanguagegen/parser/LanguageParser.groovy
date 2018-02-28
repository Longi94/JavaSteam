package in.dragonbra.steamlanguagegen.parser

import in.dragonbra.steamlanguagegen.parser.token.Token
import in.dragonbra.steamlanguagegen.parser.token.TokenSourceInfo

import java.util.regex.Pattern

class LanguageParser {

    private static final Pattern PATTERN = Pattern.compile('' +
            /(?<whitespace>\s+)|/ +
            /(?<terminator>[;])|/ +
            /["](?<string>.+?)["]|/ +
            /\/\/(?<comment>.*)$|/ +
            /(?<identifier>-?[a-zA-Z_0-9][a-zA-Z0-9_:.]*)|/ +
            /[#](?<preprocess>[a-zA-Z]*)|/ +
            /(?<operator>[{}<>\]=|])|/ +
            /(?<invalid>[^\s]+)/, Pattern.MULTILINE)

    static final GROUP_NAMES = [
        'whitespace', 'terminator', 'string', 'comment', 'identifier', 'preprocess', 'operator', 'invalid'
    ]

    static Queue<Token> tokenizeString(String buffer, String fileName) {
        def bufferLines = buffer.split("[\\r\\n]+")

        def tokens = new ArrayDeque<Token>()

        for (int i = 0; i < bufferLines.length; i++) {
            def line = bufferLines[i]

            def matcher = PATTERN.matcher(line)

            while (matcher.find()) {
                String matchValue = null
                String groupName = null

                for (String tempName : GROUP_NAMES) {
                    matchValue = matcher.group(tempName)
                    groupName = tempName
                    if (matchValue != null) {
                        break
                    }
                }

                if (matchValue == null || 'comment' == groupName || 'whitespace' == groupName) {
                    continue
                }

                def startColumnNumber = line.indexOf(matchValue)
                def endColumnNumber = line.indexOf(matchValue) + matchValue.length()

                def source = new TokenSourceInfo(fileName, i, startColumnNumber, i, endColumnNumber)
                def token = new Token(groupName, matchValue, source)

                tokens << token
            }
        }

        return tokens
    }
}
