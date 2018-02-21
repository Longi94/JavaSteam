package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;

import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientEmailAddrInfo implements ISteamSerializableMessage {

    private long passwordStrength = 0L;

    private long flagsAccountSecurityPolicy = 0L;

    private boolean validated = false;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientEmailAddrInfo;
    }

    public long getPasswordStrength() {
        return this.passwordStrength;
    }

    public void setPasswordStrength(long passwordStrength) {
        this.passwordStrength = passwordStrength;
    }

    public long getFlagsAccountSecurityPolicy() {
        return this.flagsAccountSecurityPolicy;
    }

    public void setFlagsAccountSecurityPolicy(long flagsAccountSecurityPolicy) {
        this.flagsAccountSecurityPolicy = flagsAccountSecurityPolicy;
    }

    public boolean getValidated() {
        return this.validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
    }

    @Override
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
