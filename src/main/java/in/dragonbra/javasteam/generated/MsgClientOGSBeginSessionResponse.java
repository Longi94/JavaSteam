package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;

import java.io.*;

public class MsgClientOGSBeginSessionResponse implements ISteamSerializableMessage {

    private EResult result = EResult.from(0);

    private boolean collectingAny = false;

    private boolean collectingDetails = false;

    private long sessionId = 0L;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientOGSBeginSessionResponse;
    }

    public EResult getResult() {
        return this.result;
    }

    public void setResult(EResult result) {
        this.result = result;
    }

    public boolean getCollectingAny() {
        return this.collectingAny;
    }

    public void setCollectingAny(boolean collectingAny) {
        this.collectingAny = collectingAny;
    }

    public boolean getCollectingDetails() {
        return this.collectingDetails;
    }

    public void setCollectingDetails(boolean collectingDetails) {
        this.collectingDetails = collectingDetails;
    }

    public long getSessionId() {
        return this.sessionId;
    }

    public void setSessionId(long sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeInt(result.code());
        dos.writeBoolean(collectingAny);
        dos.writeBoolean(collectingDetails);
        dos.writeLong(sessionId);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);

        result = EResult.from(dis.readInt());
        collectingAny = dis.readBoolean();
        collectingDetails = dis.readBoolean();
        sessionId = dis.readLong();
    }
}
