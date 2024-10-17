package in.dragonbra.javasteam.util;

import in.dragonbra.javasteam.TestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * @author lngtr
 * @since 2018-04-16
 */
public class WebHelpersTest extends TestBase {

    @Test
    public void urlEncodeWithString() {
        String result = WebHelpers.urlEncode("encrypt THIS sTrInG1234 \10 \11 \12");
        Assertions.assertEquals("encrypt+THIS+sTrInG1234+%08+%09+%0A", result);
    }

    @Test
    public void urlEncodeWithByteArray() {
        var input = "encrypt THIS sTrInG1234 \10 \11 \12".getBytes();
        String result = WebHelpers.urlEncode(input);
        Assertions.assertEquals("encrypt+THIS+sTrInG1234+%08+%09+%0A", result);
    }
}
