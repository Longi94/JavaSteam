package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EChatAction;
import in.dragonbra.javasteam.enums.EChatActionResult;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.SteamID;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientChatActionResult implements ISteamSerializableMessage {

    private long steamIdChat = 0L;

    private long steamIdUserActedOn = 0L;

    private EChatAction chatAction = EChatAction.from(0);

    private EChatActionResult actionResult = EChatActionResult.from(0);

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientChatActionResult;
    }

    public SteamID getSteamIdChat() {
        return new SteamID(this.steamIdChat);
    }

    public void setSteamIdChat(SteamID steamId) {
        this.steamIdChat = steamId.convertToUInt64();
    }

    public SteamID getSteamIdUserActedOn() {
        return new SteamID(this.steamIdUserActedOn);
    }

    public void setSteamIdUserActedOn(SteamID steamId) {
        this.steamIdUserActedOn = steamId.convertToUInt64();
    }

    public EChatAction getChatAction() {
        return this.chatAction;
    }

    public void setChatAction(EChatAction chatAction) {
        this.chatAction = chatAction;
    }

    public EChatActionResult getActionResult() {
        return this.actionResult;
    }

    public void setActionResult(EChatActionResult actionResult) {
        this.actionResult = actionResult;
    }

    @Override
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
