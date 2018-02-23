package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.*;

public class MsgClientJustStrings implements ISteamSerializableMessage {

    @Override
    public EMsg getEMsg() {
        return EMsg.Invalid;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

    }
}
