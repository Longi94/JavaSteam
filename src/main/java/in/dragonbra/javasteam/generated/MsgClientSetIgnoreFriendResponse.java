package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeLong(friendId);
        bw.writeInt(result.code());
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        friendId = br.readLong();
        result = EResult.from(br.readInt());
    }
}
