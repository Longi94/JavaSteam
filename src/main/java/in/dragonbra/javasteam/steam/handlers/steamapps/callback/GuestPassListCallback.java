package in.dragonbra.javasteam.steam.handlers.steamapps.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.generated.MsgClientUpdateGuestPassesList;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.KeyValue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This callback is received when the list of guest passes is updated.
 */
public class GuestPassListCallback extends CallbackMsg {

    private EResult result;

    private int countGuestPassesToGive;

    private int countGuestPassesToRedeem;

    private List<KeyValue> guestPasses;

    public GuestPassListCallback(MsgClientUpdateGuestPassesList msg, InputStream payload) {
        result = msg.getResult();
        countGuestPassesToGive = msg.getCountGuestPassesToGive();
        countGuestPassesToRedeem = msg.getCountGuestPassesToRedeem();

        guestPasses = new ArrayList<>();
        try {
            for (int i = 0; i < countGuestPassesToGive + countGuestPassesToRedeem; i++) {
                KeyValue kv = new KeyValue();
                kv.tryReadAsBinary(payload);
                guestPasses.add(kv);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("failed to read guest passes", e);
        }
    }

    /**
     * @return the result of the operation.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the number of guest passes to be given out.
     */
    public int getCountGuestPassesToGive() {
        return countGuestPassesToGive;
    }

    /**
     * @return the number of guest passes to be redeemed.
     */
    public int getCountGuestPassesToRedeem() {
        return countGuestPassesToRedeem;
    }

    /**
     *
     * @return the guest pass list.
     */
    public List<KeyValue> getGuestPasses() {
        return guestPasses;
    }
}
