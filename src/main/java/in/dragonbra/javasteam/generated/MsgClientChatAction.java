package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EChatAction;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientChatAction implements ISteamSerializableMessage {

    private long steamIdChat = 0L;

    private long steamIdUserToActOn = 0L;

    private EChatAction chatAction = EChatAction.from(0);

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientChatAction;
    }

    public SteamID getSteamIdChat() {
        return new SteamID(this.steamIdChat);
    }

    public void setSteamIdChat(SteamID steamId) {
        this.steamIdChat = steamId.convertToUInt64();
    }

    public SteamID getSteamIdUserToActOn() {
        return new SteamID(this.steamIdUserToActOn);
    }

    public void setSteamIdUserToActOn(SteamID steamId) {
        this.steamIdUserToActOn = steamId.convertToUInt64();
    }

    public EChatAction getChatAction() {
        return this.chatAction;
    }

    public void setChatAction(EChatAction chatAction) {
        this.chatAction = chatAction;
    }

    @Override
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
