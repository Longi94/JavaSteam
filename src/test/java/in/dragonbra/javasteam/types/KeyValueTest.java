package in.dragonbra.javasteam.types;

import in.dragonbra.javasteam.TestBase;
import in.dragonbra.javasteam.enums.EChatPermission;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.util.stream.MemoryStream;
import in.dragonbra.javasteam.util.stream.SeekOrigin;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.EOFException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.UUID;

/**
 * @author lngtr
 * @since 2018-02-26
 */
public class KeyValueTest extends TestBase {

    public static final String TEST_OBJECT_HEX = "00546573744F626A65637400016B65790076616C7565000808";

    @TempDir
    Path folder;

    @Test
    public void keyValueInitializesCorrectly() {
        KeyValue kv = new KeyValue("name", "value");

        Assertions.assertEquals("name", kv.getName());
        Assertions.assertEquals("value", kv.getValue());
        Assertions.assertTrue(kv.getChildren().isEmpty());
    }

    @Test
    public void keyValueIndexerReturnsValidAndInvalid() {
        KeyValue kv = new KeyValue();

        kv.getChildren().add(new KeyValue("exists", "value"));

        Assertions.assertEquals("value", kv.get("exists").getValue());
        Assertions.assertEquals(KeyValue.INVALID, kv.get("thiskeydoesntexist"));
    }

    @Test
    public void keyValueIndexerDoesntallowDuplicates() {
        KeyValue kv = new KeyValue();

        kv.set("key", new KeyValue());

        Assertions.assertEquals(1, kv.getChildren().size());

        kv.set("key", new KeyValue());

        Assertions.assertEquals(1, kv.getChildren().size());

        kv.set("key2", new KeyValue());

        Assertions.assertEquals(2, kv.getChildren().size());
    }

    @Test
    public void keyValueIndexerUpdatesKey() {
        KeyValue kv = new KeyValue();

        KeyValue subkey = new KeyValue();

        Assertions.assertNull(subkey.getName());

        kv.set("subkey", subkey);

        Assertions.assertEquals("subkey", subkey.getName());
        Assertions.assertEquals("subkey", kv.get("subkey").getName());
    }

    @Test
    public void keyValueLoadsFromString() {
        KeyValue kv = KeyValue.loadFromString("" +
                "\"root\"" +
                "{" +
                "    \"name\" \"value\"" +
                "    \"subkey\"" +
                "    {" +
                "        \"name2\" \"value2\"" +
                "    }" +
                "}");

        Assertions.assertEquals("root", kv.getName());
        Assertions.assertNull(kv.getValue());

        Assertions.assertEquals("name", kv.get("name").getName());
        Assertions.assertEquals("value", kv.get("name").getValue());
        Assertions.assertEquals(0, kv.get("name").getChildren().size());

        KeyValue subKey = kv.get("subkey");

        Assertions.assertEquals(1, subKey.getChildren().size());

        Assertions.assertEquals("name2", subKey.get("name2").getName());
        Assertions.assertEquals("value2", subKey.get("name2").getValue());
        Assertions.assertEquals(0, subKey.get("name2").getChildren().size());
    }

    @Test
    public void keyValuesMissingKeysGiveInvalid() {
        KeyValue kv = new KeyValue();
        Assertions.assertSame(KeyValue.INVALID, kv.get("missingkey"));
    }

    @Test
    public void keyValuesKeysAreCaseInsensitive() {
        KeyValue kv = KeyValue.loadFromString("" +
                "\"root\"" +
                "{" +
                "    \"name\" \"value\"" +
                "}");

        Assertions.assertEquals("value", kv.get("name").getValue());
        Assertions.assertEquals("value", kv.get("NAME").getValue());
        Assertions.assertEquals("value", kv.get("NAme").getValue());
    }

    @Test
    public void keyValuesHandlesBool() {
        KeyValue kv = KeyValue.loadFromString("" +
                "\"root\"" +
                "{" +
                "    \"name\" \"1\"" +
                "}");

        Assertions.assertTrue(kv.get("name").asBoolean());

        kv.get("name").setValue("0");
        Assertions.assertFalse(kv.get("name").asBoolean());

        kv.get("name").setValue("1000");
        Assertions.assertTrue(kv.get("name").asBoolean(), "values other than 0 are truthy");

        kv.get("name").setValue("inavlidbool");
        Assertions.assertFalse(kv.get("name").asBoolean(), "values that cannot be converted to integers are falsey");
    }

    @Test
    public void keyValuesHandlesFloat() {
        KeyValue kv = KeyValue.loadFromString("" +
                "\"root\"" +
                "{" +
                "    \"name\" \"123.456\"" +
                "}");

        Assertions.assertEquals(123.456f, kv.get("name").asFloat(), 0.0f);

        kv.get("name").setValue("invalidfloat");

        Assertions.assertEquals(654.321f, kv.get("name").asFloat(654.321f), 0.0f);
    }

    @Test
    public void keyValuesHandlesInteger() {
        KeyValue kv = KeyValue.loadFromString("" +
                "\"root\"" +
                "{" +
                "    \"name\" \"123\"" +
                "}");

        Assertions.assertEquals(123, kv.get("name").asInteger());

        kv.get("name").setValue("invalidint");

        Assertions.assertEquals(321, kv.get("name").asInteger(321));
    }

    @Test
    public void keyValuesHandlesLong() {
        KeyValue kv = KeyValue.loadFromString("" +
                "\"root\"" +
                "{" +
                "    \"name\" \"-5001050759734897745\"" +
                "}");

        Assertions.assertEquals(-5001050759734897745L, kv.get("name").asLong());

        kv.get("name").setValue("invalidint");

        Assertions.assertEquals(678L, kv.get("name").asLong(678L));
    }

    @Test
    public void keyValuesHandlesString() {
        KeyValue kv = KeyValue.loadFromString("" +
                "\"root\"" +
                "{" +
                "    \"name\" \"stringvalue\"" +
                "}");

        Assertions.assertEquals("stringvalue", kv.get("name").asString());
        Assertions.assertEquals("stringvalue", kv.get("name").getValue());
    }

    @Test
    public void keyValuesWritesBinaryToFile() throws IOException {
        final Path tempFile = Files.createFile(folder.resolve("keyValuesWritesBinaryToFile.txt"));

        String expectedHexValue = "00525000017374617475730023444F54415F52505F424F54505241435449434500016E756D5F706172616D730030000" +
                "17761746368696E675F736572766572005B413A313A323130383933353136393A353431325D00017761746368696E675F66726F6D5F73" +
                "6572766572005B413A313A3836343436383939343A353431325D000808";

        KeyValue kv = new KeyValue("RP");
        kv.getChildren().add(new KeyValue("status", "#DOTA_RP_BOTPRACTICE"));
        kv.getChildren().add(new KeyValue("num_params", "0"));
        kv.getChildren().add(new KeyValue("watching_server", "[A:1:2108935169:5412]"));
        kv.getChildren().add(new KeyValue("watching_from_server", "[A:1:864468994:5412]"));

        kv.saveToFile(tempFile.toFile(), true);

        byte[] binaryValue = Files.readAllBytes(Paths.get(tempFile.toFile().getPath()));
        String hexValue = Hex.encodeHexString(binaryValue, false).replaceAll("-", "");

        Assertions.assertEquals(expectedHexValue, hexValue);
    }

    @Test
    public void keyValuesWritesBinaryToStream() {
        String expectedHexValue = "00525000017374617475730023444F54415F52505F424F54505241435449434500016E756D5F706172616D730030000" +
                "17761746368696E675F736572766572005B413A313A323130383933353136393A353431325D00017761746368696E675F66726F6D5F73" +
                "6572766572005B413A313A3836343436383939343A353431325D000808";

        KeyValue kv = new KeyValue("RP");
        kv.getChildren().add(new KeyValue("status", "#DOTA_RP_BOTPRACTICE"));
        kv.getChildren().add(new KeyValue("num_params", "0"));
        kv.getChildren().add(new KeyValue("watching_server", "[A:1:2108935169:5412]"));
        kv.getChildren().add(new KeyValue("watching_from_server", "[A:1:864468994:5412]"));

        try (MemoryStream ms = new MemoryStream()) {
            kv.saveToStream(ms.asOutputStream(), true);

            String hexValue = Hex.encodeHexString(ms.toByteArray(), false).replaceAll("-", "");

            Assertions.assertEquals(expectedHexValue, hexValue);
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void keyValueBinarySerializationIsSymmetric() throws IOException {
        KeyValue kv = new KeyValue("MessageObject");
        kv.getChildren().add(new KeyValue("key", "value"));

        KeyValue deserializedKv = new KeyValue();
        boolean loaded;

        MemoryStream ms = new MemoryStream();
        kv.saveToStream(ms.asOutputStream(), true);
        ms.seek(0, SeekOrigin.BEGIN);
        loaded = deserializedKv.tryReadAsBinary(ms);

        Assertions.assertTrue(loaded);

        Assertions.assertEquals(kv.getName(), deserializedKv.getName());
        Assertions.assertEquals(kv.getChildren().size(), deserializedKv.getChildren().size());

        for (int i = 0; i < kv.getChildren().size(); i++) {
            KeyValue originalChild = kv.getChildren().get(i);
            KeyValue deserializedChild = deserializedKv.getChildren().get(i);

            Assertions.assertEquals(originalChild.getName(), deserializedChild.getName());
            Assertions.assertEquals(originalChild.getValue(), deserializedChild.getValue());
        }
    }

    @Test
    public void keyValues_TryReadAsBinary_ReadsBinary() throws IOException, DecoderException {
        byte[] binary = Hex.decodeHex(TEST_OBJECT_HEX);
        KeyValue kv = new KeyValue();
        boolean success;

        MemoryStream ms = new MemoryStream(binary);
        success = kv.tryReadAsBinary(ms);
        Assertions.assertEquals(ms.getLength(), ms.getPosition());

        Assertions.assertTrue(success, "Should have read test object.");
        Assertions.assertEquals("TestObject", kv.getName());
        Assertions.assertEquals(1, kv.getChildren().size());
        Assertions.assertEquals("key", kv.getChildren().get(0).getName());
        Assertions.assertEquals("value", kv.getChildren().get(0).getValue());
    }

    @Test
    public void keyValuesReadsBinaryWithLeftoverData() throws IOException, DecoderException {
        byte[] binary = Hex.decodeHex(TEST_OBJECT_HEX + UUID.randomUUID().toString().replaceAll("-", ""));
        KeyValue kv = new KeyValue();
        boolean success;

        MemoryStream ms = new MemoryStream(binary);
        success = kv.tryReadAsBinary(ms);
        Assertions.assertEquals(TEST_OBJECT_HEX.length() / 2, ms.getPosition());
        Assertions.assertEquals(16, ms.getLength() - ms.getPosition());

        Assertions.assertTrue(success, "Should have read test object.");
        Assertions.assertEquals("TestObject", kv.getName());
        Assertions.assertEquals(1, kv.getChildren().size());
        Assertions.assertEquals("key", kv.getChildren().get(0).getName());
        Assertions.assertEquals("value", kv.getChildren().get(0).getValue());
    }

    @Test
    public void keyValuesFailsToReadTruncatedBinary() throws IOException, DecoderException {
        // Test every possible truncation boundary we have.
        for (int i = 0; i < TEST_OBJECT_HEX.length(); i += 2) {
            byte[] binary = Hex.decodeHex(TEST_OBJECT_HEX.substring(0, i));
            KeyValue kv = new KeyValue();
            boolean success = false;

            MemoryStream ms = new MemoryStream(binary);
            try {
                success = kv.tryReadAsBinary(ms);
                Assertions.fail();
            } catch (EOFException ignored) {
            }
            Assertions.assertEquals(ms.getLength(), ms.getPosition());

            Assertions.assertFalse(success, "Should not have read test object.");
        }
    }

    @Test
    public void keyValuesReadsBinaryWithMultipleChildren() throws IOException, DecoderException {
        String hex = "00546573744f626a65637400016b6579310076616c75653100016b6579320076616c756532000808";
        byte[] binary = Hex.decodeHex(hex);
        KeyValue kv = new KeyValue();
        boolean success;
        MemoryStream ms = new MemoryStream(binary);
        success = kv.tryReadAsBinary(ms);

        Assertions.assertTrue(success);

        Assertions.assertEquals("TestObject", kv.getName());
        Assertions.assertEquals(2, kv.getChildren().size());
        Assertions.assertEquals("key1", kv.getChildren().get(0).getName());
        Assertions.assertEquals("value1", kv.getChildren().get(0).getValue());
        Assertions.assertEquals("key2", kv.getChildren().get(1).getName());
        Assertions.assertEquals("value2", kv.getChildren().get(1).getValue());
    }

    @Test
    public void keyValuesSavesTextToFile() throws IOException {
        final Path tempFile = Files.createFile(folder.resolve("keyValuesSavesTextToFile.txt"));

        String expected = "\"RootNode\"\n{\n\t\"key1\"\t\t\"value1\"\n\t\"key2\"\n\t{\n\t\t\"ChildKey\"\t\t\"ChildValue\"\n\t}\n}\n";

        KeyValue kv = new KeyValue("RootNode");
        KeyValue kv2 = new KeyValue("key2");
        kv2.getChildren().add(new KeyValue("ChildKey", "ChildValue"));

        kv.getChildren().add(new KeyValue("key1", "value1"));
        kv.getChildren().add(kv2);

        String text;
        kv.saveToFile(tempFile.toFile(), false);
        text = new String(Files.readAllBytes(Paths.get(tempFile.toFile().getPath())));

        Assertions.assertEquals(expected, text);
    }

    @Test
    public void keyValuesSavesTextToStream() {
        String expected = "\"RootNode\"\n{\n\t\"key1\"\t\t\"value1\"\n\t\"key2\"\n\t{\n\t\t\"ChildKey\"\t\t\"ChildValue\"\n\t}\n}\n";

        KeyValue kv = new KeyValue("RootNode");
        KeyValue kv2 = new KeyValue("key2");
        kv2.getChildren().add(new KeyValue("ChildKey", "ChildValue"));

        kv.getChildren().add(new KeyValue("key1", "value1"));
        kv.getChildren().add(kv2);

        String text = saveToText(kv);
        Assertions.assertEquals(expected, text);
    }

    @Test
    public void canLoadUnicodeTextDocument() {
        String expected = "\"RootNode\"\n{\n\t\"key1\"\t\t\"value1\"\n\t\"key2\"\n\t{\n\t\t\"ChildKey\"\t\t\"ChildValue\"\n\t}\n}\n";
        KeyValue kv = new KeyValue();

        try {
            Path tempFile = Files.createFile(folder.resolve("tempFile.txt"));

            Files.writeString(tempFile, expected, StandardCharsets.UTF_8);

            kv.readFileAsText(tempFile.toString());

            Assertions.assertEquals("RootNode", kv.getName());
            Assertions.assertEquals(2, kv.getChildren().size());
            Assertions.assertEquals("key1", kv.getChildren().get(0).getName());
            Assertions.assertEquals("value1", kv.getChildren().get(0).getValue());
            Assertions.assertEquals("key2", kv.getChildren().get(1).getName());
            Assertions.assertEquals(1, kv.getChildren().get(1).getChildren().size());
            Assertions.assertEquals("ChildKey", kv.getChildren().get(1).getChildren().get(0).getName());
            Assertions.assertEquals("ChildValue", kv.getChildren().get(1).getChildren().get(0).getValue());
        } catch (IOException e) {
            Assertions.fail(e);
        }
    }

    @Test
    public void canLoadUnicodeTextStream() {
        var expected = "\"RootNode\"\n{\n\t\"key1\"\t\t\"value1\"\n\t\"key2\"\n\t{\n\t\t\"ChildKey\"\t\t\"ChildValue\"\n\t}\n}\n";
        var kv = new KeyValue();

        try {
            final Path tempFile = Files.createFile(folder.resolve("canLoadUnicodeTextStream.txt"));

            Files.writeString(tempFile, expected, StandardCharsets.UTF_8);

            try (var fs = Files.newInputStream(tempFile)) {
                kv.readAsText(fs);
            }

            Assertions.assertEquals("RootNode", kv.getName());
            Assertions.assertEquals(2, kv.getChildren().size());
            Assertions.assertEquals("key1", kv.getChildren().get(0).getName());
            Assertions.assertEquals("value1", kv.getChildren().get(0).getValue());
            Assertions.assertEquals("key2", kv.getChildren().get(1).getName());
            Assertions.assertEquals(1, kv.getChildren().get(1).getChildren().size());
            Assertions.assertEquals("ChildKey", kv.getChildren().get(1).getChildren().get(0).getName());
            Assertions.assertEquals("ChildValue", kv.getChildren().get(1).getChildren().get(0).getValue());
        } catch (IOException e) {
            Assertions.fail(e);
        }
    }

    // @Test
    // public void canReadAndIgnoreConditionals() {
    // }

    @Test
    public void writesNewLineAsSlashN() {
        KeyValue kv = new KeyValue("abc");
        kv.getChildren().add(new KeyValue("def", "ghi\njkl"));

        String text = saveToText(kv);
        String expected = ("\n" +
                "\"abc\"\n" +
                "{\n" + "\t" + "\"def\"\t\t\"ghi\\njkl\"\n" +
                "}\n").trim().replace("\r\n", "\n") + "\n";

        Assertions.assertEquals(expected, text);
    }

    @Test
    public void keyValuesUnsignedByteConversion() {
        byte expectedValue = 37;

        KeyValue kv = new KeyValue("key", "37");
        Assertions.assertEquals(expectedValue, kv.asByte());

        kv.setValue("256");
        Assertions.assertEquals(expectedValue, kv.asByte(expectedValue));
    }

    @Test
    public void keyValuesUnsignedShortConversion() {
        short expectedValue = 1337;

        KeyValue kv = new KeyValue("key", "1337");
        Assertions.assertEquals(expectedValue, kv.asShort());

        kv.setValue("123456");
        Assertions.assertEquals(expectedValue, kv.asShort(expectedValue));
    }

    @Test
    public void keyValuesEscapesTextWhenSerializing() {
        KeyValue kv = new KeyValue("key");
        kv.getChildren().add(new KeyValue("slashes", "\\o/"));
        kv.getChildren().add(new KeyValue("newline", "\r\n"));

        String text = saveToText(kv);

        String expectedValue = "\"key\"\n{\n\t\"slashes\"\t\t\"\\\\o/\"\n\t\"newline\"\t\t\"\\r\\n\"\n}\n";
        Assertions.assertEquals(expectedValue, text);
    }

    @Test
    public void keyValuesTextPreserveEmptyObjects() {
        KeyValue kv = new KeyValue("key");
        kv.getChildren().add(new KeyValue("emptyObj"));
        kv.getChildren().add(new KeyValue("emptyString", ""));

        String text = saveToText(kv);

        String expectedValue = "\"key\"\n{\n\t\"emptyObj\"\n\t{\n\t}\n\t\"emptyString\"\t\t\"\"\n}\n";
        Assertions.assertEquals(expectedValue, text);
    }

    @Test
    public void keyValuesBinaryPreserveEmptyObjects() {
        String expectedHexString = "006B65790000656D7074794F626A000801656D707479537472696E6700000808";

        KeyValue kv = new KeyValue("key");
        kv.getChildren().add(new KeyValue("emptyObj"));
        kv.getChildren().add(new KeyValue("emptyString", ""));

        KeyValue deserializedKv = new KeyValue();
        byte[] binaryValue = new byte[0];
        try (var ms = new MemoryStream()) {
            kv.saveToStream(ms.asOutputStream(), true);
            ms.seek(0, SeekOrigin.BEGIN);
            binaryValue = ms.toByteArray();
            deserializedKv.tryReadAsBinary(ms);
        } catch (IOException e) {
            Assertions.fail(e);
        }

        String hexValue = Hex.encodeHexString(binaryValue).toUpperCase();

        Assertions.assertEquals(expectedHexString, hexValue);
        Assertions.assertNull(deserializedKv.get("emptyObj").getValue());
        Assertions.assertTrue(deserializedKv.get("emptyObj").getChildren().isEmpty());
        Assertions.assertEquals("", deserializedKv.get("emptyString").getValue());
    }

    @Test
    public void decodesBinaryWithFieldType10() {
        var hex = "00546573744F626A656374000A6B65790001020304050607080808";
        byte[] binary = null;
        try {
            binary = Hex.decodeHex(hex);
        } catch (DecoderException e) {
            Assertions.fail(e);
        }
        var kv = new KeyValue();
        try (var ms = new MemoryStream(binary)) {
            var read = kv.tryReadAsBinary(ms);
            Assertions.assertTrue(read);
        } catch (IOException e) {
            Assertions.fail(e);
        }

        Assertions.assertEquals(0x0807060504030201L, kv.get("key").asLong());
    }

    @Test
    public void keyValuesHandlesEnum() {
        KeyValue kv = KeyValue.loadFromString("" +
                "\"root\"" +
                "{" +
                "    \"name\" \"Talk\"" +
                "}");

        Assertions.assertEquals(EnumSet.of(EChatPermission.Talk), kv.get("name").asEnum(EChatPermission.class, EChatPermission.EveryoneDefault));

        kv.get("name").setValue("8");

        Assertions.assertEquals(EnumSet.of(EChatPermission.Talk), kv.get("name").asEnum(EChatPermission.class, EChatPermission.EveryoneDefault));

        kv.get("name").setValue("10");

        Assertions.assertEquals(EnumSet.of(EChatPermission.Talk, EChatPermission.Invite), kv.get("name").asEnum(EChatPermission.class, EChatPermission.EveryoneDefault));
        Assertions.assertEquals(EnumSet.of(EResult.Busy), kv.get("name").asEnum(EResult.class, EResult.Invalid));

        kv.get("name").setValue("OwnerDefault");

        Assertions.assertEquals(EChatPermission.OwnerDefault, kv.get("name").asEnum(EChatPermission.class, EChatPermission.EveryoneDefault));
    }

    @Test
    public void keyValues_loadAsText_should_read_successfully() {
        URL file = this.getClass().getClassLoader().getResource("textkeyvalues/appinfo_utf8.txt");

        Assertions.assertNotNull(file, "Resource file was null");

        KeyValue kv = KeyValue.loadAsText(file.getPath());

        Assertions.assertEquals("1234567", kv.get("appid").getValue(), "appid should be 1234567");
        Assertions.assertEquals(2, kv.getChildren().size(), "Children should be 2");
    }

    private static String saveToText(KeyValue kv) {
        String text = null;

        try (MemoryStream ms = new MemoryStream()) {
            kv.saveToStream(ms.asOutputStream(), false);
            ms.seek(0, SeekOrigin.BEGIN);
            text = new String(ms.toByteArray());
        } catch (IOException e) {
            Assertions.fail(e);
        }

        return text;
    }
}
