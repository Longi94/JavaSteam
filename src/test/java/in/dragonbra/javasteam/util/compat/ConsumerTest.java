package in.dragonbra.javasteam.util.compat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConsumerTest {

    @Test
    void testConsumerString() {
        Consumer<String> consumer = s -> Assertions.assertEquals("Test String", s);

        consumer.accept("Test String");
    }

    @Test
    void testConsumerBoolean() {
        Consumer<Boolean> consumer = b -> Assertions.assertEquals(true, b);

        consumer.accept(true);
    }
}
