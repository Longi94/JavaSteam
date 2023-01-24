package in.dragonbra.javasteam.util.compat;

import in.dragonbra.javasteam.TestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObjectsCompatTest extends TestBase {

    @SuppressWarnings("ConstantValue")
    @Test
    public void testEquals() {
        final String a = "aaa";
        final String b = "bbb";
        final String c = null;

        assertFalse(ObjectsCompat.equals(a, b));
        assertFalse(ObjectsCompat.equals(a, c));
        assertFalse(ObjectsCompat.equals(c, a));

        assertTrue(ObjectsCompat.equals(a, a));
        assertTrue(ObjectsCompat.equals(b, b));
        assertTrue(ObjectsCompat.equals(c, c));
    }

}