package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;

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
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
