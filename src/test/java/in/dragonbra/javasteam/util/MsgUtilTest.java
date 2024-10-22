package in.dragonbra.javasteam.util;

import in.dragonbra.javasteam.enums.EMsg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MsgUtilTest {

    @Test
    public void isProtoBuf() {
        var result = MsgUtil.isProtoBuf(-2147482868); // ClientLicenseList
        Assertions.assertTrue(result);
    }

    @Test
    public void isNotProtoBuf() {
        var result = MsgUtil.isProtoBuf(798); // ClientUpdateGuestPassesList
        Assertions.assertFalse(result);
    }

    @Test
    public void getMsgAsServiceMethodResponse() {
        var result = MsgUtil.getMsg(-2147483501); // ServiceMethodResponse
        Assertions.assertEquals(EMsg.ServiceMethodResponse, result);
    }

    @Test
    public void getMsgAsClientUpdateGuestPassesList() {
        var result = MsgUtil.getMsg(798); // ClientUpdateGuestPassesList
        Assertions.assertEquals(EMsg.ClientUpdateGuestPassesList, result);
    }

    @Test
    public void getMsgAsWrongMsg() {
        var result = MsgUtil.getMsg(-2147483501); // ServiceMethodResponse
        Assertions.assertNotEquals(EMsg.ClientUpdateGuestPassesList, result);
    }
}
