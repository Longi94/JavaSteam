package in.dragonbra.javasteam;

import in.dragonbra.javasteam.util.log.DefaultLogListener;
import in.dragonbra.javasteam.util.log.LogManager;
import org.junit.jupiter.api.BeforeAll;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public abstract class TestBase {
    @BeforeAll
    public static void beforeClass() {
        LogManager.addListener(new DefaultLogListener());
    }
}
