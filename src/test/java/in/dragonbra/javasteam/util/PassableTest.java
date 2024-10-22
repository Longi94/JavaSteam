package in.dragonbra.javasteam.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PassableTest {

    @Test
    public void BooleanPassable() {
        var passableValue = new Passable<>(false);

        Assertions.assertFalse(passableValue.getValue());

        passableValue.setValue(true);
        Assertions.assertTrue(passableValue.getValue());

        passableValue.setValue(null);
        Assertions.assertNull(passableValue.getValue());
    }

    @Test
    public void IntegerPassable() {
        var passableValue = new Passable<Integer>(null);

        Assertions.assertNull(passableValue.getValue());

        passableValue.setValue(1);
        Assertions.assertEquals(1, passableValue.getValue());

        passableValue.setValue(2);
        Assertions.assertEquals(2, passableValue.getValue());
    }
}
