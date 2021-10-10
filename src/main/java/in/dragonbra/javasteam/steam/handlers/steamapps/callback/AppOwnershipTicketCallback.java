package in.dragonbra.javasteam.steam.handlers.steamapps.callback;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientGetAppOwnershipTicketResponse;
import in.dragonbra.javasteam.steam.handlers.steamapps.SteamApps;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.JobID;

/**
 * This callback is received in response to calling {@link SteamApps#getAppOwnershipTicket(int)}
 */
public class AppOwnershipTicketCallback extends CallbackMsg {

    private EResult result;

    private int appID;

    private byte[] ticket;

    public AppOwnershipTicketCallback(JobID jobID, CMsgClientGetAppOwnershipTicketResponse.Builder msg) {
        setJobID(jobID);

        result = EResult.from(msg.getEresult());
        appID = msg.getAppId();
        ticket = msg.getTicket().toByteArray();
    }

    /**
     * @return the result of requesting the ticket.
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the AppID this ticket is for.
     */
    public int getAppID() {
        return appID;
    }

    /**
     * @return the ticket data.
     */
    public byte[] getTicket() {
        return ticket;
    }
}
