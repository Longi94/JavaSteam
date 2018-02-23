package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientUpdateGuestPassesList implements ISteamSerializableMessage {

    private EResult result = EResult.from(0);

    private int countGuestPassesToGive = 0;

    private int countGuestPassesToRedeem = 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientUpdateGuestPassesList;
    }

    public EResult getResult() {
        return this.result;
    }

    public void setResult(EResult result) {
        this.result = result;
    }

    public int getCountGuestPassesToGive() {
        return this.countGuestPassesToGive;
    }

    public void setCountGuestPassesToGive(int countGuestPassesToGive) {
        this.countGuestPassesToGive = countGuestPassesToGive;
    }

    public int getCountGuestPassesToRedeem() {
        return this.countGuestPassesToRedeem;
    }

    public void setCountGuestPassesToRedeem(int countGuestPassesToRedeem) {
        this.countGuestPassesToRedeem = countGuestPassesToRedeem;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeInt(result.code());
        bw.writeInt(countGuestPassesToGive);
        bw.writeInt(countGuestPassesToRedeem);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        result = EResult.from(br.readInt());
        countGuestPassesToGive = br.readInt();
        countGuestPassesToRedeem = br.readInt();
    }
}
