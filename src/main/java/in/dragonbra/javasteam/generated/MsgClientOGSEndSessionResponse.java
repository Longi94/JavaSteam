package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.*;

public class MsgClientOGSEndSessionResponse implements ISteamSerializableMessage {

    private EResult result = EResult.from(0);

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientOGSEndSessionResponse;
    }

    public EResult getResult() {
        return this.result;
    }

    public void setResult(EResult result) {
        this.result = result;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeInt(result.code());
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        result = EResult.from(br.readInt());
    }
}
