package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.*;

public class MsgClientLoggedOff implements ISteamSerializableMessage {

    private EResult result = EResult.from(0);

    private int secMinReconnectHint = 0;

    private int secMaxReconnectHint = 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientLoggedOff;
    }

    public EResult getResult() {
        return this.result;
    }

    public void setResult(EResult result) {
        this.result = result;
    }

    public int getSecMinReconnectHint() {
        return this.secMinReconnectHint;
    }

    public void setSecMinReconnectHint(int secMinReconnectHint) {
        this.secMinReconnectHint = secMinReconnectHint;
    }

    public int getSecMaxReconnectHint() {
        return this.secMaxReconnectHint;
    }

    public void setSecMaxReconnectHint(int secMaxReconnectHint) {
        this.secMaxReconnectHint = secMaxReconnectHint;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeInt(result.code());
        dos.writeInt(secMinReconnectHint);
        dos.writeInt(secMaxReconnectHint);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        result = EResult.from(br.readInt());
        secMinReconnectHint = br.readInt();
        secMaxReconnectHint = br.readInt();
    }
}
