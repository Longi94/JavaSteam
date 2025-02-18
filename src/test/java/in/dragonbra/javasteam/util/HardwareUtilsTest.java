package in.dragonbra.javasteam.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

public class HardwareUtilsTest {

    @BeforeEach
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        // Ehh.... This resets the 'MACHINE_NAME' field for every test.
        Field field = HardwareUtils.class.getDeclaredField("MACHINE_NAME");
        field.setAccessible(true);
        field.set(null, null);
    }

    @Test
    public void machineNameWithTag() {
        var name = HardwareUtils.getMachineName(true);
        System.out.println(name);
        Assertions.assertTrue(name.contains(" (JavaSteam)"));
    }

    @Test
    public void machineNameWithNoTag() {
        var name = HardwareUtils.getMachineName();
        System.out.println(name);
        Assertions.assertFalse(name.contains(" (JavaSteam)"));
    }
}
