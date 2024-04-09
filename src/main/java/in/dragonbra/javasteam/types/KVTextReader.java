package in.dragonbra.javasteam.types;

import in.dragonbra.javasteam.util.Passable;
import in.dragonbra.javasteam.util.Strings;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
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
        } while (!endOfStream());
    }

    private void eatWhiteSpace() throws IOException {
        while (!endOfStream()) {
            if (!Character.isWhitespace((char) peek())) {
                break;
            }

            read();
        }
    }

    private boolean eatCPPComment() throws IOException {
        if (!endOfStream()) {
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
        } while (c != '\n' && !endOfStream());
    }

    private byte peek() throws IOException {
        int p = read();
        if (p >= 0) {
            unread(p);
        }
        return (byte) p;
    }

    public String readToken(Passable<Boolean> wasQuoted, Passable<Boolean> wasConditional) throws IOException {
        wasQuoted.setValue(false);
        wasConditional.setValue(false);

        while (true) {
            eatWhiteSpace();

            if (endOfStream()) {
                return null;
            }

            if (!eatCPPComment()) {
                break;
            }
        }

        if (endOfStream()) {
            return null;
        }

        char next = (char) peek();
        if (next == '"') {
            wasQuoted.setValue(true);

            // "
            read();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            while (!endOfStream()) {
                if (peek() == '\\') {
                    read();

                    char escapedChar = (char) read();

                    Character replacedChar = ESCAPED_MAPPING.get(escapedChar);
                    if (replacedChar == null) {
                        replacedChar = escapedChar;
                    }

                    baos.write(replacedChar);

                    continue;
                }

                if (peek() == '"') {
                    break;
                }

                baos.write(read());
            }

            // "
            read();

            // convert the output stream as an utf-8 supported string.
            return baos.toString(StandardCharsets.UTF_8);
        }

        if (next == '{' || next == '}') {
            read();
            return String.valueOf(next);
        }

        boolean bConditionalStart = false;
        int count = 0;
        StringBuilder ret = new StringBuilder();

        while (!endOfStream()) {
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

    private boolean endOfStream() {
        try {
            return peek() == -1;
        } catch (IOException e) {
            return true;
        }
    }
}
