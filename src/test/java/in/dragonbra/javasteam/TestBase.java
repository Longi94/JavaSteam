package in.dragonbra.javasteam;

import in.dragonbra.javasteam.util.log.DefaultLogListener;
import in.dragonbra.javasteam.util.log.LogManager;
import org.junit.BeforeClass;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public abstract class TestBase {
    @BeforeClass
    public static void beforeClass() {
        LogManager.addListener(new DefaultLogListener());
    }
}
