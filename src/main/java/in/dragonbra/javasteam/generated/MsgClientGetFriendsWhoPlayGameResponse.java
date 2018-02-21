package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.types.GameID;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientGetFriendsWhoPlayGameResponse implements ISteamSerializableMessage {

    private EResult result = EResult.from(0);

    private long gameId = 0L;

    private long countFriends = 0L;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientGetFriendsWhoPlayGameResponse;
    }

    public EResult getResult() {
        return this.result;
    }

    public void setResult(EResult result) {
        this.result = result;
    }

    public GameID getGameId() {
        return new GameID(this.gameId);
    }

    public void setGameId(GameID gameId) {
        this.gameId = gameId.convertToUInt64();
    }

    public long getCountFriends() {
        return this.countFriends;
    }

    public void setCountFriends(long countFriends) {
        this.countFriends = countFriends;
    }

    @Override
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
