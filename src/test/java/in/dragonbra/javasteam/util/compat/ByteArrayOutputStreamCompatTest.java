package in.dragonbra.javasteam.util.compat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class ByteArrayOutputStreamCompatTest {

    @Test
    public void testEmptyStream() {
        var baos = new ByteArrayOutputStream();

        var compatResult = ByteArrayOutputStreamCompat.toString(baos);
        var standardResult = baos.toString();

        Assertions.assertEquals("", compatResult);
        Assertions.assertEquals("", standardResult);
        Assertions.assertEquals(standardResult, compatResult);
    }

    @Test
    public void testAsciiContent() {
        var baos = new ByteArrayOutputStream();
        var testString = "Hello, World!";

        baos.write(testString.getBytes(StandardCharsets.UTF_8), 0, testString.length());

        var compatResult = ByteArrayOutputStreamCompat.toString(baos);
        var standardResult = baos.toString();

        Assertions.assertEquals(testString, compatResult);
        Assertions.assertEquals(testString, standardResult);
        Assertions.assertEquals(standardResult, compatResult);
    }

    @Test
    public void testUnicodeContent() {
        var baos = new ByteArrayOutputStream();
        var testString = "Hello, 世界! 👋";
        var bytes = testString.getBytes(StandardCharsets.UTF_8);

        baos.write(bytes, 0, bytes.length);

        var compatResult = ByteArrayOutputStreamCompat.toString(baos);
        var standardResult = baos.toString();

        Assertions.assertEquals(testString, compatResult);
        Assertions.assertEquals(testString, standardResult);
        Assertions.assertEquals(standardResult, compatResult);
    }

    @Test
    public void testLargeContent() {
        var baos = new ByteArrayOutputStream();
        var largeString = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeString.append("Line ").append(i).append("\n");
        }
        var testString = largeString.toString();
        var bytes = testString.getBytes(StandardCharsets.UTF_8);
        baos.write(bytes, 0, bytes.length);

        var compatResult = ByteArrayOutputStreamCompat.toString(baos);
        var standardResult = baos.toString();

        Assertions.assertEquals(testString, compatResult);
        Assertions.assertEquals(testString, standardResult);
        Assertions.assertEquals(standardResult, compatResult);
    }

    @Test
    public void testPartialWrites() {
        var baos = new ByteArrayOutputStream();
        var part1 = "Hello";
        var part2 = ", ";
        var part3 = "World!";

        baos.write(part1.getBytes(StandardCharsets.UTF_8), 0, part1.length());
        baos.write(part2.getBytes(StandardCharsets.UTF_8), 0, part2.length());
        baos.write(part3.getBytes(StandardCharsets.UTF_8), 0, part3.length());

        var expected = part1 + part2 + part3;
        var compatResult = ByteArrayOutputStreamCompat.toString(baos);
        var standardResult = baos.toString();

        Assertions.assertEquals(expected, compatResult);
        Assertions.assertEquals(expected, standardResult);
        Assertions.assertEquals(standardResult, compatResult);
    }

}
