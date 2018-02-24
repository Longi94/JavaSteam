package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;
import in.dragonbra.javasteam.util.stream.BinaryReader;
import in.dragonbra.javasteam.util.stream.BinaryWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MsgClientMarketingMessageUpdate2 implements ISteamSerializableMessage {

    private int marketingMessageUpdateTime = 0;

    private int count = 0;

    @Override
    public EMsg getEMsg() {
        return EMsg.ClientMarketingMessageUpdate2;
    }

    public int getMarketingMessageUpdateTime() {
        return this.marketingMessageUpdateTime;
    }

    public void setMarketingMessageUpdateTime(int marketingMessageUpdateTime) {
        this.marketingMessageUpdateTime = marketingMessageUpdateTime;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public void serialize(OutputStream stream) throws IOException {
        BinaryWriter bw = new BinaryWriter(stream);

        bw.writeInt(marketingMessageUpdateTime);
        bw.writeInt(count);
    }

    @Override
    public void deserialize(InputStream stream) throws IOException {
        BinaryReader br = new BinaryReader(stream);

        marketingMessageUpdateTime = br.readInt();
        count = br.readInt();
    }
}
