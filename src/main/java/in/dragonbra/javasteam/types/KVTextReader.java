package in.dragonbra.javasteam.types;

import in.dragonbra.javasteam.util.Passable;
import in.dragonbra.javasteam.util.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author lngtr
 * @since 2018-02-26
 */
public class KVTextReader extends PushbackInputStream {

    public static final Map<Character, Character> ESCAPED_MAPPING;

    static {
        Map<Character, Character> escapedMapping = new TreeMap<>();

        escapedMapping.put('n', '\n');
        escapedMapping.put('r', '\r');
        escapedMapping.put('t', '\t');
        escapedMapping.put('\\', '\\');

        ESCAPED_MAPPING = Collections.unmodifiableMap(escapedMapping);
    }

    KVTextReader(KeyValue kv, InputStream is) throws IOException {
        super(is);
        Passable<Boolean> wasQuoted = new Passable<>(false);
        Passable<Boolean> wasConditional = new Passable<>(false);

        KeyValue currentKey = kv;

        do {
            String s = readToken(wasQuoted, wasConditional);

            if (Strings.isNullOrEmpty(s)) {
                break;
            }

            if (currentKey == null) {
                currentKey = new KeyValue(s);
            } else {
                currentKey.setName(s);
            }

            s = readToken(wasQuoted, wasConditional);

            if (wasConditional.getValue()) {
                // Now get the '{'
                s = readToken(wasQuoted, wasConditional);
            }

            if (s.startsWith("{") && !wasQuoted.getValue()) {
                // header is valid so load the file
                currentKey.recursiveLoadFromBuffer(this);
            } else {
                throw new IllegalStateException("LoadFromBuffer: missing {");
            }

            currentKey = null;
        } while (available() > 0);
    }

    private void eatWhiteSpace() throws IOException {
        while (available() > 0) {
            if (!Character.isWhitespace((char) peek())) {
                break;
            }

            read();
        }
    }

    private boolean eatCPPComment() throws IOException {
        if (available() > 0) {
            char next = (char) peek();

            if (next == '/') {
                readLine();
                return true;
                /*
                 *  As came up in parsing the Dota 2 units.txt file, the reference (Valve) implementation
                 *  of the KV format considers a single forward slash to be sufficient to comment out the
                 *  entirety of a line. While they still _tend_ to use two, it's not required, and likely
                 *  is just done out of habit.
                 */
            }

            return false;
        }
        return false;
    }

    private void readLine() throws IOException {
        char c;
        do {
            c = (char) read();
        } while (c != '\n' && available() > 0);
    }

    private byte peek() throws IOException {
        int p = read();
        unread(p);
        return (byte) p;
    }

    public String readToken(Passable<Boolean> wasQuoted, Passable<Boolean> wasConditional) throws IOException {
        wasQuoted.setValue(false);
        wasConditional.setValue(false);

        while (true) {
            eatWhiteSpace();

            if (available() == 0) {
                return null;
            }

            if (!eatCPPComment()) {
                break;
            }
        }

        if (available() == 0) {
            return null;
        }

        char next = (char) peek();
        if (next == '"') {
            wasQuoted.setValue(true);

            // "
            read();

            StringBuilder sb = new StringBuilder();

            while (available() > 0) {
                if (peek() == '\\') {
                    read();

                    char escapedChar = (char) read();
                    char replacedChar = ESCAPED_MAPPING.getOrDefault(escapedChar, escapedChar);

                    sb.append(replacedChar);

                    continue;
                }

                if (peek() == '"') {
                    break;
                }

                sb.append((char) read());
            }

            // "
            read();

            return sb.toString();
        }

        if (next == '{' || next == '}') {
            read();
            return String.valueOf(next);
        }

        boolean bConditionalStart = false;
        int count = 0;
        StringBuilder ret = new StringBuilder();

        while (available() > 0) {
            next = (char) peek();

            if (next == '"' || next == '{' || next == '}') {
                break;
            }

            if (next == '[') {
                bConditionalStart = true;
            }

            if (next == ']' && bConditionalStart) {
                wasConditional.setValue(true);
            }

            if (Character.isWhitespace(next)) {
                break;
            }

            if (count < 1023) {
                ret.append(next);
            } else {
                throw new IOException("ReadToken overflow");
            }

            read();
        }
        return ret.toString();
    }
}
