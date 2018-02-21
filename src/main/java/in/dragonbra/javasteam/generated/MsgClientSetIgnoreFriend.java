package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientSetIgnoreFriend implements ISteamSerializableMessage {

    private long mySteamId = 0L;

    private long steamIdFriend = 0L;

    private byte ignore = (byte) 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientSetIgnoreFriend;
    }

    public SteamID getMySteamId() {
        return new SteamID(this.mySteamId);
    }

    public void setMySteamId(SteamID steamId) {
        this.mySteamId = steamId.convertToUInt64();
    }

    public SteamID getSteamIdFriend() {
        return new SteamID(this.steamIdFriend);
    }

    public void setSteamIdFriend(SteamID steamId) {
        this.steamIdFriend = steamId.convertToUInt64();
    }

    public byte getIgnore() {
        return this.ignore;
    }

    public void setIgnore(byte ignore) {
        this.ignore = ignore;
    }

    @Override
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
