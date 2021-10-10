package in.dragonbra.javasteam.steam.handlers.steamapps.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver2.CMsgClientGetDepotDecryptionKeyResponse;
import in.dragonbra.javasteam.steam.handlers.steamapps.SteamApps;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is received in response to calling {@link SteamApps#getDepotDecryptionKey(int, int)}
 */
public class DepotKeyCallback extends CallbackMsg {

    private EResult result;

    private int depotID;

    private byte[] depotKey;

    public DepotKeyCallback(JobID jobID, CMsgClientGetDepotDecryptionKeyResponse.Builder msg) {
        setJobID(jobID);

        result = EResult.from(msg.getEresult());
        depotID = msg.getDepotId();
        depotKey = msg.getDepotEncryptionKey().toByteArray();
    }

    /**
     * @return the result of requesting this encryption key.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the DepotID this encryption key is for.
     */
    public int getDepotID() {
        return depotID;
    }

    /**
     * @return the encryption key for this depot.
     */
    public byte[] getDepotKey() {
        return depotKey;
    }
}
