package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientNewLoginKey implements ISteamSerializableMessage {

    private int uniqueID = 0;

    private byte[] loginKey = new byte[20];

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientNewLoginKey;
    }

    public int getUniqueID() {
        return this.uniqueID;
    }

    public void setUniqueID(int uniqueID) {
        this.uniqueID = uniqueID;
    }

    public byte[] getLoginKey() {
        return this.loginKey;
    }

    public void setLoginKey(byte[] loginKey) {
        this.loginKey = loginKey;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeInt(uniqueID);
        bw.writeInt(loginKey.length);
        bw.write(loginKey);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        uniqueID = br.readInt();
        loginKey = br.readBytes(br.readInt());
    }
}
