package in.dragonbra.javasteam.generated;

import in.dragonbra.javasteam.base.ISteamSerializableMessage;
import in.dragonbra.javasteam.enums.EMsg;

import java.io.InputStream;
import java.io.OutputStream;

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
    public void serialize(OutputStream stream) {
    }

    @Override
    public void deserialize(InputStream stream) {
    }
}
