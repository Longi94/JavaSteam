package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.*;

public class MsgGSGetUserGroupStatus implements ISteamSerializableMessage {

    private long steamIdUser = 0L;

    private long steamIdGroup = 0L;

    @Override
    public EMsg getEMsg() {
        return EMsg.GSGetUserGroupStatus;
    }

    public SteamID getSteamIdUser() {
        return new SteamID(this.steamIdUser);
    }

    public void setSteamIdUser(SteamID steamId) {
        this.steamIdUser = steamId.convertToUInt64();
    }

    public SteamID getSteamIdGroup() {
        return new SteamID(this.steamIdGroup);
    }

    public void setSteamIdGroup(SteamID steamId) {
        this.steamIdGroup = steamId.convertToUInt64();
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeLong(steamIdUser);
        dos.writeLong(steamIdGroup);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        steamIdUser = br.readLong();
        steamIdGroup = br.readLong();
    }
}
