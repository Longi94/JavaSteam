package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.*;

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
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeLong(gameId);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        gameId = br.readLong();
    }
}
