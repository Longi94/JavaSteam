package in.dragonbra.javasteam.util.compat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import in.dragonbra.javasteam.TestBase;

public class ObjectsCompatTest extends TestBase {

    @Test
    public void testEquals() throws Exception {
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