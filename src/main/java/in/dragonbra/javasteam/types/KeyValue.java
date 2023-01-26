package in.dragonbra.javasteam.types;

import in.dragonbra.javasteam.util.Passable;
import in.dragonbra.javasteam.util.Strings;
import in.dragonbra.javasteam.util.log.LogManager;
import in.dragonbra.javasteam.util.log.Logger;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.MemoryStream;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Represents a recursive string key to arbitrary value container.
 */
@SuppressWarnings("unchecked")
public class KeyValue {

    private static final Logger logger = LogManager.getLogger(KeyValue.class);

    /**
     * Represents an invalid {@link KeyValue} given when a searched for child does not exist.
     */
    public static final KeyValue INVALID = new KeyValue();

    private String name;

    private String value;

    private List<KeyValue> children = new ArrayList<>();

    /**
     * Initializes a new instance of the {@link KeyValue} class.
     */
    public KeyValue() {
        this(null);
    }

    /**
     * Initializes a new instance of the {@link KeyValue} class.
     *
     * @param name The optional name of the root key.
     */
    public KeyValue(String name) {
        this(name, null);
    }

    /**
     * Initializes a new instance of the {@link KeyValue} class.
     *
     * @param name  The optional name of the root key.
     * @param value The optional value assigned to the root key.
     */
    public KeyValue(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Gets the child {@link KeyValue} with the specified key.
     * If no child with the given key exists, {@link KeyValue#INVALID} is returned.
     *
     * @param key key
     * @return the child {@link KeyValue}
     */
    public KeyValue get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        for (KeyValue c : children) {
            if (key.equalsIgnoreCase(c.name)) {
                return c;
            }
        }
        return INVALID;
    }

    /**
     * Sets the child {@link KeyValue} with the specified key.
     *
     * @param key   key
     * @param value the child {@link KeyValue}
     */
    public void set(String key, KeyValue value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }

        children.removeIf(keyValue -> key.equalsIgnoreCase(keyValue.name));

        value.setName(key);
        children.add(value);
    }

    /**
     * Returns the value of this instance as a string.
     *
     * @return The value of this instance as a string.
     */
    public String asString() {
        return value;
    }

    /**
     * Attempts to convert and return the value of this instance as a byte.
     * If the conversion is invalid, the default value is returned.
     *
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an unsigned byte.
     */
    public byte asByte(byte defaultValue) {
        try {
            return Byte.parseByte(value);
        } catch (NullPointerException | NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * Attempts to convert and return the value of this instance as a byte.
     * If the conversion is invalid, the default value is returned.
     *
     * @return The value of this instance as an unsigned byte.
     */
    public byte asByte() {
        return asByte((byte) 0);
    }

    /**
     * Attempts to convert and return the value of this instance as a short.
     * If the conversion is invalid, the default value is returned.
     *
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an unsigned byte.
     */
    public short asShort(short defaultValue) {
        try {
            return Short.parseShort(value);
        } catch (NullPointerException | NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * Attempts to convert and return the value of this instance as a short.
     * If the conversion is invalid, the default value is returned.
     *
     * @return The value of this instance as an unsigned byte.
     */
    public short asShort() {
        return asShort((short) 0);
    }

    /**
     * Attempts to convert and return the value of this instance as an integer.
     * If the conversion is invalid, the default value is returned.
     *
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an unsigned byte.
     */
    public int asInteger(int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NullPointerException | NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * Attempts to convert and return the value of this instance as an integer.
     * If the conversion is invalid, the default value is returned.
     *
     * @return The value of this instance as an unsigned byte.
     */
    public int asInteger() {
        return asInteger(0);
    }

    /**
     * Attempts to convert and return the value of this instance as a long.
     * If the conversion is invalid, the default value is returned.
     *
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an unsigned byte.
     */
    public long asLong(long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NullPointerException | NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * Attempts to convert and return the value of this instance as a long.
     * If the conversion is invalid, the default value is returned.
     *
     * @return The value of this instance as an unsigned byte.
     */
    public long asLong() {
        return asLong(0L);
    }

    /**
     * Attempts to convert and return the value of this instance as a float.
     * If the conversion is invalid, the default value is returned.
     *
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an unsigned byte.
     */
    public float asFloat(float defaultValue) {
        try {
            return Float.parseFloat(value);
        } catch (NullPointerException | NumberFormatException nfe) {
            return defaultValue;
        }
    }

    /**
     * Attempts to convert and return the value of this instance as a float.
     * If the conversion is invalid, the default value is returned.
     *
     * @return The value of this instance as an unsigned byte.
     */
    public float asFloat() {
        return asFloat(0.0f);
    }

    /**
     * Attempts to convert and return the value of this instance as a boolean.
     * If the conversion is invalid, the default value is returned.
     *
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an unsigned byte.
     */
    public boolean asBoolean(boolean defaultValue) {
        try {
            return Integer.parseInt(value) != 0;
        } catch (NullPointerException | NumberFormatException e) {
            try {
                return Boolean.parseBoolean(value);
            } catch (NullPointerException | NumberFormatException e1) {
                return defaultValue;
            }
        }
    }

    /**
     * Attempts to convert and return the value of this instance as a boolean.
     * If the conversion is invalid, the default value is returned.
     *
     * @return The value of this instance as an unsigned byte.
     */
    public boolean asBoolean() {
        return asBoolean(false);
    }

    /**
     * Attempts to convert and return the value of this instance as an enum.
     * If the conversion is invalid, the default value is returned.
     *
     * @param <T>          the type of the enum to convert to
     * @param enumClass    the type of the enum to convert to
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an unsigned byte.
     */
    public <T extends Enum<T>> EnumSet<T> asEnum(Class<T> enumClass, T defaultValue) {
        return asEnum(enumClass, EnumSet.of(defaultValue));
    }

    /**
     * Attempts to convert and return the value of this instance as an enum.
     * If the conversion is invalid, the default value is returned.
     *
     * @param <T>          the type of the enum to convert to
     * @param enumClass    the type of the enum to convert to
     * @param defaultValue The default value to return if the conversion is invalid.
     * @return The value of this instance as an unsigned byte.
     */
    public <T extends Enum<T>> EnumSet<T> asEnum(Class<T> enumClass, EnumSet<T> defaultValue) {
        // this is ugly af, but it comes with handling bit flags as enumsets
        try {
            // see if it's a number first
            int code = Integer.parseInt(value);

            Field codeField = enumClass.getDeclaredField("code");
            Method from = enumClass.getMethod("from", codeField.getType());
            Object res = from.invoke(null, code);

            if (res instanceof EnumSet) {
                return (EnumSet<T>) res;
            } else {
                return EnumSet.of(enumClass.cast(res));
            }
        } catch (NullPointerException | NumberFormatException ignored) {
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }

        try {
            // see if it exists as an enum
            return EnumSet.of(T.valueOf(enumClass, value));
        } catch (NullPointerException | IllegalArgumentException ignored) {
        }

        // check for static enumset fields
        try {
            for (Field field : enumClass.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) && field.getName().equals(value) && field.getType().isAssignableFrom(EnumSet.class)) {
                    return (EnumSet<T>) field.get(null);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<KeyValue> getChildren() {
        return children;
    }

    public boolean readAsText(InputStream is) throws IOException {
        if (is == null) {
            throw new IllegalArgumentException("input stream is null");
        }

        children = new ArrayList<>();

        new KVTextReader(this, is);

        return true;
    }

    /**
     * Opens and reads the given filename as text.
     *
     * @param filename The file to open and read.
     * @return <b>true</b> if the read was successful; otherwise, <b>false</b>.
     * @throws IOException exception while reading from the file
     */
    public boolean readFileAsText(String filename) throws IOException {
        try (FileInputStream fis = new FileInputStream(filename)) {
            return readAsText(fis);
        }
    }

    void recursiveLoadFromBuffer(KVTextReader kvr) throws IOException {
        Passable<Boolean> wasQuoted = new Passable<>(false);
        Passable<Boolean> wasConditional = new Passable<>(false);

        while (true) {
            // get the key name
            String name = kvr.readToken(wasQuoted, wasConditional);

            if (Strings.isNullOrEmpty(name)) {
                throw new IllegalStateException("RecursiveLoadFromBuffer: got EOF or empty keyname");
            }

            if (name.startsWith("}") && !wasQuoted.getValue()) {
                break;
            }

            KeyValue dat = new KeyValue(name);
            dat.children = new ArrayList<>();
            children.add(dat);

            String value = kvr.readToken(wasQuoted, wasConditional);

            if (value == null) {
                throw new IllegalStateException("RecursiveLoadFromBuffer:  got NULL key");
            }

            if (value.startsWith("}") && !wasQuoted.getValue()) {
                throw new IllegalStateException("RecursiveLoadFromBuffer:  got } in key");
            }

            if (value.startsWith("{") && !wasQuoted.getValue()) {
                dat.recursiveLoadFromBuffer(kvr);
            } else {
                if (wasConditional.getValue()) {
                    throw new IllegalStateException("RecursiveLoadFromBuffer:  got conditional between key and value");
                }

                dat.setValue(value);
            }
        }
    }

    /**
     * Attempts to load the given filename as a text {@link KeyValue}.
     *
     * @param path The path to the file to load.
     * @return a {@link KeyValue} instance if the load was successful, or <b>null</b> on failure.
     */
    public static KeyValue loadAsText(String path) {
        return loadFromFile(path, false);
    }

    /**
     * Attempts to load the given filename as a binary {@link KeyValue}.
     *
     * @param path The path to the file to load.
     * @return a {@link KeyValue} instance if the load was successful, or <b>null</b> on failure.
     */
    public static KeyValue tryLoadAsBinary(String path) {
        return loadFromFile(path, true);
    }

    private static KeyValue loadFromFile(String path, boolean asBinary) {
        File file = new File(path);

        if (!file.exists() || file.isDirectory()) {
            return null;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            // Massage the incoming file to be encoded as UTF-8.
            String fisString = IOUtils.toString(fis, Charset.defaultCharset());
            byte[] fisStringToBytes = fisString.getBytes(StandardCharsets.UTF_8);
            MemoryStream ms = new MemoryStream(fisStringToBytes, 0, fisStringToBytes.length - 1);

            KeyValue kv = new KeyValue();

            if (asBinary) {
                if (!kv.tryReadAsBinary(ms)) {
                    return null;
                }
            } else {
                if (!kv.readAsText(ms)) {
                    return null;
                }
            }

            return kv;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Attempts to create an instance of {@link KeyValue} from the given input text.
     *
     * @param input The input text to load.
     * @return a {@link KeyValue} instance if the load was successful, or <b>null</b> on failure.
     */
    public static KeyValue loadFromString(String input) {
        if (input == null) {
            throw new IllegalArgumentException("input is null");
        }

        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            KeyValue kv = new KeyValue();

            if (!kv.readAsText(bais)) {
                return null;
            }

            return kv;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Saves this instance to file.
     *
     * @param path   The file path to save to.
     * @param binary If set to <b>true</b>, saves this instance as binary.
     * @throws IOException exception while writing to the file
     */
    public void saveToFile(File path, boolean binary) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path, false)) {
            saveToStream(fos, binary);
        }
    }

    public void saveToStream(OutputStream os, boolean binary) throws IOException {
        if (os == null) {
            throw new IllegalArgumentException("output stream is null");
        }

        if (binary) {
            recursiveSaveBinaryToStream(os);
        } else {
            recursiveSaveTextToFile(os);
        }
    }

    private void recursiveSaveBinaryToStream(OutputStream os) throws IOException {
        recursiveSaveBinaryToStreamCore(os);
        os.write(Type.END.code());
    }

    private void recursiveSaveBinaryToStreamCore(OutputStream os) throws IOException {
        // Only supported types ATM:
        // 1. KeyValue with children (no value itself)
        // 2. String KeyValue
        if (value == null) {
            os.write(Type.NONE.code());
            os.write(name.getBytes(StandardCharsets.UTF_8));
            os.write(0);
            for (KeyValue child : children) {
                child.recursiveSaveBinaryToStreamCore(os);
            }
            os.write(Type.END.code());
        } else {
            os.write(Type.STRING.code());
            os.write(name.getBytes(StandardCharsets.UTF_8));
            os.write(0);
            os.write(value.getBytes(StandardCharsets.UTF_8));
            os.write(0);
        }
    }

    private void recursiveSaveTextToFile(OutputStream os) throws IOException {
        recursiveSaveTextToFile(os, 0);
    }

    private void recursiveSaveTextToFile(OutputStream os, int indentLevel) throws IOException {
        // write header
        writeIndents(os, indentLevel);
        writeString(os, name, true);
        writeString(os, "\n");
        writeIndents(os, indentLevel);
        writeString(os, "{\n");

        // loop through all our keys writing them to disk
        for (KeyValue child : children) {
            if (child.getValue() == null) {
                child.recursiveSaveTextToFile(os, indentLevel + 1);
            } else {
                writeIndents(os, indentLevel + 1);
                writeString(os, child.getName(), true);
                writeString(os, "\t\t");
                writeString(os, escapeText(child.asString()), true);
                writeString(os, "\n");
            }
        }

        writeIndents(os, indentLevel);
        writeString(os, "}\n");
    }

    private static String escapeText(String value) {
        for (Map.Entry<Character, Character> entry : KVTextReader.ESCAPED_MAPPING.entrySet()) {
            String textToReplace = String.valueOf(entry.getValue());
            String escapedReplacement = "\\" + entry.getKey();
            value = value.replace(textToReplace, escapedReplacement);
        }

        return value;
    }

    private void writeIndents(OutputStream os, int indentLevel) throws IOException {
        writeString(os, new String(new char[indentLevel]).replace('\0', '\t'));
    }

    private static void writeString(OutputStream os, String str) throws IOException {
        writeString(os, str, false);
    }

    private static void writeString(OutputStream os, String str, boolean quote) throws IOException {
        str = str.replaceAll("\"", "\\\"");
        if (quote) {
            str = "\"" + str + "\"";
        }
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        os.write(bytes);
    }

    /**
     * Populate this instance from the given {@link InputStream} as a binary {@link KeyValue}.
     *
     * @param is The input {@link InputStream} to read from.
     * @return <b>true</b> if the read was successful; otherwise, <b>false</b>.
     * @throws IOException exception while reading from the stream
     */
    public boolean tryReadAsBinary(InputStream is) throws IOException {
        if (is == null) {
            throw new IllegalArgumentException("input stream is null");
        }

        return tryReadAsBinaryCore(is, this, null);
    }

    @SuppressWarnings("DuplicateBranchesInSwitch")
    private static boolean tryReadAsBinaryCore(InputStream is, KeyValue current, KeyValue parent) throws IOException {
        current.children = new ArrayList<>();

        BinaryReader br = new BinaryReader(is);

        while (true) {
            Type type = Type.from(br.readByte());

            if (type == Type.END) {
                break;
            }

            current.setName(br.readNullTermString(StandardCharsets.UTF_8));
            switch (type) {
                case NONE:
                    KeyValue child = new KeyValue();
                    boolean didReadChild = tryReadAsBinaryCore(is, child, current);
                    if (!didReadChild) {
                        return false;
                    }
                    break;
                case STRING:
                    current.setValue(br.readNullTermString(StandardCharsets.UTF_8));
                    break;
                case WIDESTRING:
                    logger.debug("Encountered WideString type when parsing binary KeyValue, which is unsupported. Returning false.");
                    return false;
                case INT32:
                case COLOR:
                case POINTER:
                    current.setValue(String.valueOf(br.readInt()));
                    break;
                case UINT64:
                    current.setValue(String.valueOf(br.readLong()));
                    break;
                case FLOAT32:
                    current.setValue(String.valueOf(br.readFloat()));
                    break;
                case INT64:
                    current.setValue(String.valueOf(br.readLong()));
                    break;
                default:
                    return false;
            }

            if (parent != null) {
                parent.getChildren().add(current);
            }
            current = new KeyValue();
        }

        return true;
    }

    @Override
    public String toString() {
        return String.format("%s = %s", name, value);
    }

    public enum Type {
        NONE((byte) 0),
        STRING((byte) 1),
        INT32((byte) 2),
        FLOAT32((byte) 3),
        POINTER((byte) 4),
        WIDESTRING((byte) 5),
        COLOR((byte) 6),
        UINT64((byte) 7),
        END((byte) 8),
        INT64((byte) 10);

        private final byte code;

        Type(byte code) {
            this.code = code;
        }

        public byte code() {
            return this.code;
        }

        public static Type from(byte code) {
            for (Type e : Type.values()) {
                if (e.code == code) {
                    return e;
                }
            }
            return null;
        }
    }
}
