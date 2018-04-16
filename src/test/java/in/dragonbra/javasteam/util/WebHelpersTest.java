package in.dragonbra.javasteam.util;

import in.dragonbra.javasteam.TestBase;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author lngtr
 * @since 2018-04-16
 */
public class WebHelpersTest extends TestBase {

    @Test
    public void urlEncode() {
        String result = WebHelpers.urlEncode("encrypt THIS sTrInG1234 \10 \11 \12");
        Assert.assertEquals("encrypt+THIS+sTrInG1234+%08+%09+%0A", result);
    }
}