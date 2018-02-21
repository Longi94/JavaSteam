package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.types.SteamID;

import java.io.*;

public class MsgClientSetIgnoreFriendResponse implements ISteamSerializableMessage {

    private long friendId = 0L;

    private EResult result = EResult.from(0);

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientSetIgnoreFriendResponse;
    }

    public SteamID getFriendId() {
        return new SteamID(this.friendId);
    }

    public void setFriendId(SteamID steamId) {
        this.friendId = steamId.convertToUInt64();
    }

    public EResult getResult() {
        return this.result;
    }

    public void setResult(EResult result) {
        this.result = result;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeLong(friendId);
        dos.writeInt(result.code());
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);

        friendId = dis.readLong();
        result = EResult.from(dis.readInt());
    }
}
