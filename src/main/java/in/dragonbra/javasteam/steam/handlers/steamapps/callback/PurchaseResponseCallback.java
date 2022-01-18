package in.dragonbra.javasteam.steam.handlers.steamapps.callback;

import in.dragonbra.javasteam.enums.EPurchaseResultDetail;
import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientPurchaseResponse;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;
import in.dragonbra.javasteam.types.KeyValue;
import in.dragonbra.javasteam.util.stream.MemoryStream;

import java.io.IOException;

public class PurchaseResponseCallback extends CallbackMsg {

    private EResult result;

    private EPurchaseResultDetail purchaseResultDetail;

    private KeyValue purchaseReceiptInfo;

    public PurchaseResponseCallback(JobID jobID, CMsgClientPurchaseResponse.Builder msg) {
        setJobID(jobID);

        this.result = EResult.from(msg.getEresult());
        this.purchaseResultDetail = EPurchaseResultDetail.from(msg.getPurchaseResultDetails());
        this.purchaseReceiptInfo = new KeyValue();

        if (msg.getPurchaseReceiptInfo() == null) {
            return;
        }

        try {
            MemoryStream ms = new MemoryStream(msg.getPurchaseReceiptInfo().toByteArray());
            this.purchaseReceiptInfo.tryReadAsBinary(ms);
        } catch (IOException exception) {
            throw new IllegalArgumentException("input stream is null");
        }
    }

    /**
     * @return Result of the operation
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return Purchase result of the operation
     */
    public EPurchaseResultDetail getPurchaseResultDetail() {
        return purchaseResultDetail;
    }

    /**
     * @return Purchase receipt of the operation
     */
    public KeyValue getPurchaseReceiptInfo() {
        return purchaseReceiptInfo;
    }
}
