package in.dragonbra.javasteam;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.BeforeClass;

/**
 * @author lngtr
 * @since 2018-02-25
 */
public class TestBase {
    @BeforeClass
    public static void beforeClass() {
        Configurator.setRootLevel(Level.DEBUG);
    }
}
