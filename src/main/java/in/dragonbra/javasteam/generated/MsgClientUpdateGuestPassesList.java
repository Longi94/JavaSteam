package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;

import java.io.*;

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
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeInt(result.code());
        dos.writeInt(countGuestPassesToGive);
        dos.writeInt(countGuestPassesToRedeem);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);

        result = EResult.from(dis.readInt());
        countGuestPassesToGive = dis.readInt();
        countGuestPassesToRedeem = dis.readInt();
    }
}
