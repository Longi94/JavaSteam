package in.dragonbra.javasteam.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HardwareUtilsTest {

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
