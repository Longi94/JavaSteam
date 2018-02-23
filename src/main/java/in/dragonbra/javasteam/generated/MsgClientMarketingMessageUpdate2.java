package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.util.stream.BinaryReader;

import java.io.*;

public class MsgClientMarketingMessageUpdate2 implements ISteamSerializableMessage {

    private long marketingMessageUpdateTime = 0L;

    private long count = 0L;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientMarketingMessageUpdate2;
    }

    public long getMarketingMessageUpdateTime() {
        return this.marketingMessageUpdateTime;
    }

    public void setMarketingMessageUpdateTime(long marketingMessageUpdateTime) {
        this.marketingMessageUpdateTime = marketingMessageUpdateTime;
    }

    public long getCount() {
        return this.count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(stream);

        dos.writeLong(marketingMessageUpdateTime);
        dos.writeLong(count);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        marketingMessageUpdateTime = br.readLong();
        count = br.readLong();
    }
}
