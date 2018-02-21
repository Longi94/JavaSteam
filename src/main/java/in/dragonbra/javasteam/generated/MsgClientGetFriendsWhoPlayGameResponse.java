package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.types.GameID;

import java.io.*;

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
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeInt(result.code());
        dos.writeLong(gameId);
        dos.writeLong(countFriends);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);

        result = EResult.from(dis.readInt());
        gameId = dis.readLong();
        countFriends = dis.readLong();
    }
}
