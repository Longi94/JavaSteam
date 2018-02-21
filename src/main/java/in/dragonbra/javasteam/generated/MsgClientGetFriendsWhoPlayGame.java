package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.GameID;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientGetFriendsWhoPlayGame implements ISteamSerializableMessage {

    private long gameId = 0L;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientGetFriendsWhoPlayGame;
    }

    public GameID getGameId() {
        return new GameID(this.gameId);
    }

    public void setGameId(GameID gameId) {
        this.gameId = gameId.convertToUInt64();
    }

    @Override
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
