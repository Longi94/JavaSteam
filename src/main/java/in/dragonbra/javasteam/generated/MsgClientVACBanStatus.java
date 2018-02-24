package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientVACBanStatus implements ISteamSerializableMessage {

    private int numBans = 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientVACBanStatus;
    }

    public int getNumBans() {
        return this.numBans;
    }

    public void setNumBans(int numBans) {
        this.numBans = numBans;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeInt(numBans);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        numBans = br.readInt();
    }
}
