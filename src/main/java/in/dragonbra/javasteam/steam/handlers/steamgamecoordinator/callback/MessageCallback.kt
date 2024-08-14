package in.dragonbra.javasteam.steam.handlers.steamgamecoordinator.callback;

import in.dragonbra.javasteam.base.IPacketGCMsg;
import in.dragonbra.javasteam.base.PacketClientGCMsg;
import in.dragonbra.javasteam.base.PacketClientGCMsgProtobuf;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgGCClient;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.util.MsgUtil;

/**
 * This callback is fired when a game coordinator message is recieved from the network.
 */
public class MessageCallback extends CallbackMsg {

    private final int eMsg;

    private final int appID;

    private final IPacketGCMsg message;

    public MessageCallback(JobID jobID, CMsgGCClient.Builder gcMsg) {
        setJobID(jobID);

        eMsg = gcMsg.getMsgtype();
        appID = gcMsg.getAppid();
        message = getPacketGCMsg(gcMsg.getMsgtype(), gcMsg.getPayload().toByteArray());
    }

    /**
     * @return the game coordinator message type
     */
    public int geteMsg() {
        return MsgUtil.getGCMsg(eMsg);
    }

    /**
     * @return the AppID of the game coordinator the message is from
     */
    public int getAppID() {
        return appID;
    }

    /**
     * @return <b>true</b> if this instance is protobuf'd; otherwise, <b>false</b>
     */
    public boolean isProto() {
        return MsgUtil.isProtoBuf(eMsg);
    }

    /**
     * @return the actual message
     */
    public IPacketGCMsg getMessage() {
        return message;
    }

    private static IPacketGCMsg getPacketGCMsg(int eMsg, byte[] data) {
        int realEMsg = MsgUtil.getGCMsg(eMsg);

        if (MsgUtil.isProtoBuf(eMsg)) {
            return new PacketClientGCMsgProtobuf(realEMsg, data);
        } else {
            return new PacketClientGCMsg(realEMsg, data);
        }
    }
}
