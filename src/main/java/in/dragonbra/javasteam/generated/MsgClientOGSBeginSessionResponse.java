package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeInt(result.code());
        bw.writeBoolean(collectingAny);
        bw.writeBoolean(collectingDetails);
        bw.writeLong(sessionId);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        result = EResult.from(br.readInt());
        collectingAny = br.readBoolean();
        collectingDetails = br.readBoolean();
        sessionId = br.readLong();
    }
}
