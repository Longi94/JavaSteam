package in.dragonbra.javasteamsamples._2extending;

import in.dragonbra.javasteam.base.ClientMsgProtobuf;
import in.dragonbra.javasteam.base.IPacketMsg;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.handlers.ClientMsgHandler;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientLogOff;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientLogonResponse;
import in.dragonbra.javasteam.steam.handlers.steamuser.SteamUser;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

/**
 * @author lossy
 * @since 2021-10-11
 */
public class MyHandler extends ClientMsgHandler {

    // define our custom callback class
    // this will pass data back to the user of the handler
    static class MyCallback extends CallbackMsg {

        private final EResult result;

        // generally we don't want user code to instantiate callback objects,
        // but rather only let handlers create them
        public MyCallback(EResult result) {
            this.result = result;
        }

        public EResult getResult() {
            return result;
        }
    }

    /**
     * JavaSteam edit:
     * <p>
     * There is a log-off bug currently. Check the to-do at {@link SteamUser#logOff()}
     * The concept of this example is still valid.
     */
    // handlers can also define functions which can send data to the steam servers
    @SuppressWarnings("unused")
    public void logOff(String user, String pass) {
        ClientMsgProtobuf<CMsgClientLogOff.Builder> logOffMessage = new ClientMsgProtobuf<>(CMsgClientLogOff.class, EMsg.ClientLogOff);
        client.send(logOffMessage);

        client.disconnect(); // JavaSteam edit here.
    }

    // some other useful function
    @SuppressWarnings("unused")
    public void doSomething() {
        // this function could send some other message or perform some other logic

        // ...
        // client.send(somethingElse); // etc
        // ...
    }

    @Override
    public void handleMsg(IPacketMsg packetMsg) {
        // this function is called when a message arrives from the Steam network
        // the SteamClient class will pass the message along to every registered ClientMsgHandler

        // the MsgType exposes the EMsg (type) of the message
        //noinspection SwitchStatementWithTooFewBranches
        switch (packetMsg.getMsgType()) {
            case ClientLogOnResponse:
                handleLogonResponse(packetMsg);
                break;
            default:
                break;
        }
    }

    private void handleLogonResponse(IPacketMsg packetMsg) {
        // in order to get at the message contents, we need to wrap the packet message
        // in an object that gives us access to the message body
        ClientMsgProtobuf<CMsgClientLogonResponse.Builder> logonResponse = new ClientMsgProtobuf<>(CMsgClientLogonResponse.class, packetMsg);

        // the raw body of the message often doesn't make use of useful types, so we need to
        // cast them to types that are prettier for the user to handle
        EResult result = EResult.from(logonResponse.getBody().getEresult());

        // our handler will simply display a message in the console, and then post our custom callback with the result of logon
        System.out.println("HandleLogonResponse: " + result);

        // post the callback to be consumed by user code
        client.postCallback(new MyCallback(result));
    }
}
