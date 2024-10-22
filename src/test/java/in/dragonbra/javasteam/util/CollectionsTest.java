package in.dragonbra.javasteam.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class CollectionsTest {

    private HashMap<Integer, String> values;

    @BeforeEach
    public void setUp() {
        values = new HashMap<>();
        values.put(1, "Some Value");
        values.put(2, "A Value");
        values.put(3, "All the values");
    }

    @Test
    public void getKeyByValueExistingValue() {
        var result = CollectionUtils.getKeyByValue(values, "A Value");
        Assertions.assertEquals(2, result);
    }

    @Test
    public void getKeyByValueNullValue() {
        var nullResult = CollectionUtils.getKeyByValue(values, "Null Value");
        Assertions.assertNull(nullResult);

        var nullResult2 = CollectionUtils.getKeyByValue(values, null);
        Assertions.assertNull(nullResult2);
    }
}
