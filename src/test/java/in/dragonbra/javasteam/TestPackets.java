package in.dragonbra.javasteam;

import in.dragonbra.javasteam.base.ClientMsg;
import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.generated.MsgClientLogOnResponse;
import in.dragonbra.javasteam.generated.MsgClientLoggedOff;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientSessionToken;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.*;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * @author lngtr
 * @since 2018-03-24
 */
public abstract class TestPackets {

    public static byte[] getPacket(EMsg msgType, boolean isProto) {
        switch (msgType) {
            case ClientLogOnResponse:
                return clientLogOnResponse(isProto);
            case ClientLoggedOff:
                return clientLoggedOff(isProto);
            case ClientNewLoginKey:
                return clientNewLoginKey();
            case ClientSessionToken:
                return clientSessionToken();
            case ClientUpdateMachineAuth:
                return clientUpdateMachineAuth();
            case ClientAccountInfo:
                return clientAccountInfo();
            case ClientWalletInfoUpdate:
                return clientWalletInfoUpdate();
            case ClientRequestWebAPIAuthenticateUserNonceResponse:
                return clientRequestWebAPIAuthenticateUserNonceResponse();
            case ClientMarketingMessageUpdate2:
                return clientMarketingMessageUpdate2();
            default:
                throw new NullPointerException();
        }
    }

    private static byte[] loadFile(String name) {
        try {
            return IOUtils.toByteArray(TestPackets.class.getClassLoader().getResourceAsStream("testpackets/" + name));
        } catch (IOException e) {
            return null;
        }
    }

    // region SteamUser

    private static byte[] clientLogOnResponse(boolean isProto) {
        if (isProto) {
            ClientMsgProtobuf<CMsgClientLogonResponse.Builder> msgProto =
                    new ClientMsgProtobuf<>(CMsgClientLogonResponse.class, EMsg.ClientLogOnResponse);

            msgProto.getBody().setEresult(EResult.OK.code());

            return msgProto.serialize();
        } else {
            ClientMsg<MsgClientLogOnResponse> msg = new ClientMsg<>(MsgClientLogOnResponse.class);

            msg.getBody().setResult(EResult.OK);

            return msg.serialize();
        }
    }

    private static byte[] clientLoggedOff(boolean isProto) {
        if (isProto) {
            ClientMsgProtobuf<CMsgClientLoggedOff.Builder> msgProto =
                    new ClientMsgProtobuf<>(CMsgClientLoggedOff.class, EMsg.ClientLoggedOff);

            msgProto.getBody().setEresult(EResult.OK.code());

            return msgProto.serialize();
        } else {
            ClientMsg<MsgClientLoggedOff> msg = new ClientMsg<>(MsgClientLoggedOff.class);

            msg.getBody().setResult(EResult.OK);

            return msg.serialize();
        }
    }

    private static byte[] clientNewLoginKey() {
        ClientMsgProtobuf<CMsgClientNewLoginKey.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientNewLoginKey.class, EMsg.ClientNewLoginKey);

        msg.getBody().setLoginKey("testloginkey");
        msg.getBody().setUniqueId(69);

        return msg.serialize();
    }

    private static byte[] clientSessionToken() {
        ClientMsgProtobuf<CMsgClientSessionToken.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientSessionToken.class, EMsg.ClientSessionToken);

        msg.getBody().setToken(123);

        return msg.serialize();
    }

    private static byte[] clientUpdateMachineAuth() {
        return loadFile("ClientUpdateMachineAuth.bin");
    }

    private static byte[] clientAccountInfo() {
        ClientMsgProtobuf<CMsgClientAccountInfo.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientAccountInfo.class, EMsg.ClientAccountInfo);

        msg.getBody().setPersonaName("testpersonaname");
        msg.getBody().setIpCountry("XX");

        return msg.serialize();
    }

    private static byte[] clientWalletInfoUpdate() {
        return loadFile("ClientWalletInfoUpdate.bin");
    }

    private static byte[] clientRequestWebAPIAuthenticateUserNonceResponse() {
        ClientMsgProtobuf<CMsgClientRequestWebAPIAuthenticateUserNonceResponse.Builder> msg =
                new ClientMsgProtobuf<>(CMsgClientRequestWebAPIAuthenticateUserNonceResponse.class, EMsg.ClientRequestWebAPIAuthenticateUserNonceResponse);

        msg.getBody().setEresult(EResult.OK.code());
        msg.getBody().setWebapiAuthenticateUserNonce("testnonce");

        return msg.serialize();
    }

    private static byte[] clientMarketingMessageUpdate2() {
        return loadFile("ClientMarketingMessageUpdate2.bin");
    }

    // endregion
}
