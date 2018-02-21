package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;

import java.io.*;

public class MsgClientJoinChat implements ISteamSerializableMessage {

    private long steamIdChat = 0L;

    private boolean isVoiceSpeaker = false;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientJoinChat;
    }

    public SteamID getSteamIdChat() {
        return new SteamID(this.steamIdChat);
    }

    public void setSteamIdChat(SteamID steamId) {
        this.steamIdChat = steamId.convertToUInt64();
    }

    public boolean getIsVoiceSpeaker() {
        return this.isVoiceSpeaker;
    }

    public void setIsVoiceSpeaker(boolean isVoiceSpeaker) {
        this.isVoiceSpeaker = isVoiceSpeaker;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeLong(steamIdChat);
        dos.writeBoolean(isVoiceSpeaker);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);

        steamIdChat = dis.readLong();
        isVoiceSpeaker = dis.readBoolean();
    }
}
