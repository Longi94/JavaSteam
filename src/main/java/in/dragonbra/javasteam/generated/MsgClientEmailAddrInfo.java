package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;

import java.io.*;

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
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeLong(passwordStrength);
        dos.writeLong(flagsAccountSecurityPolicy);
        dos.writeBoolean(validated);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);

        passwordStrength = dis.readLong();
        flagsAccountSecurityPolicy = dis.readLong();
        validated = dis.readBoolean();
    }
}
