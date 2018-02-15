package in.dragonbra.steamlanguageparser.parser;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lngtr
 * @since 2018-02-15
 */
public class LanguageParser {
    private static final Pattern PATTERN = Pattern.compile("" +
            "(?<whitespace>\\s+)|" +
            "(?<terminator>[;])|" +
            "[\"](?<string>.+?)[\"]|" +
            "\\/\\/(?<comment>.*)$|" +
            "(?<identifier>-?[a-zA-Z_0-9][a-zA-Z0-9_:.]*)|" +
            "[#](?<preprocess>[a-zA-Z]*)|" +
            "(?<operator>[{}<>\\]=|])|" +
            "(?<invalid>[^\\s]+)", Pattern.MULTILINE    );

    private static final Collection<String> GROUP_NAMES;

    static {
        List<String> groups = new ArrayList<>();
        try {
            Method namedGroupsMethod;
            namedGroupsMethod = Pattern.class.getDeclaredMethod("namedGroups");
            namedGroupsMethod.setAccessible(true);


            Map<String, Integer> namedGroups = null;
            namedGroups = (Map<String, Integer>) namedGroupsMethod.invoke(PATTERN);


            if (namedGroups != null) {
                groups.addAll(namedGroups.keySet());

            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        GROUP_NAMES = Collections.unmodifiableList(groups);
    }

    public static Queue<Token> tokenizeString(String buffer) {
        return tokenizeString(buffer, "");
    }

    public static Queue<Token> tokenizeString(String buffer, String fileName) {
        String[] bufferLines = buffer.split("[\\r\\n]+");

        Queue<Token> tokens = new ArrayDeque<>();

        for (int i = 0; i < bufferLines.length; i++) {
            String line = bufferLines[i];

            Matcher matcher = PATTERN.matcher(line);

            while (matcher.find()) {
                String matchValue = null;
                String groupName = null;

                for (String tempName : GROUP_NAMES) {
                    matchValue = matcher.group(tempName);
                    groupName = tempName;
                    if (matchValue != null) {
                        break;
                    }
                }

                if (matchValue == null || "comment".equals(groupName) || "whitespace".equals(groupName)) {
                    continue;
                }

                int startColumnNumber = line.indexOf(matchValue);
                int endColumnNumber = line.indexOf(matchValue) + matchValue.length();

                TokenSourceInfo source = new TokenSourceInfo(fileName, i, startColumnNumber, i, endColumnNumber);
                Token token = new Token(groupName, matchValue, source);

                tokens.add(token);
            }
        }

        return tokens;
    }
}
