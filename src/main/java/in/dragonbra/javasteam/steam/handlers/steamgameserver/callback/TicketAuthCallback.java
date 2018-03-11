package in.dragonbra.javasteam.steam.handlers.steamgameserver.callback;

import in.dragonbra.javasteam.enums.EAuthSessionResponse;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientTicketAuthComplete;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;
import in.dragonbra.javasteam.types.GameID;
import in.dragonbra.javasteam.types.SteamID;

/**
 * This callback is fired when ticket authentication has completed.
 */
public class TicketAuthCallback extends CallbackMsg {

    private SteamID steamID;

    private GameID gameID;

    private int state;

    private EAuthSessionResponse authSessionResponse;

    private int ticketCrc;

    private int ticketSequence;

    public TicketAuthCallback(CMsgClientTicketAuthComplete.Builder tickAuth) {
        steamID = new SteamID(tickAuth.getSteamId());
        gameID = new GameID(tickAuth.getGameId());

        state = tickAuth.getEstate();

        authSessionResponse = EAuthSessionResponse.from(tickAuth.getEauthSessionResponse());

        ticketCrc = tickAuth.getTicketCrc();
        ticketSequence = tickAuth.getTicketSequence();
    }

    /**
     * @return the SteamID the ticket auth completed for
     */
    public SteamID getSteamID() {
        return steamID;
    }

    /**
     * @return the GameID the ticket was for
     */
    public GameID getGameID() {
        return gameID;
    }

    /**
     * @return the authentication state
     */
    public int getState() {
        return state;
    }

    /**
     * @return the auth session response
     */
    public EAuthSessionResponse getAuthSessionResponse() {
        return authSessionResponse;
    }

    /**
     * @return the ticket CRC
     */
    public int getTicketCrc() {
        return ticketCrc;
    }

    /**
     * @return the ticket sequence
     */
    public int getTicketSequence() {
        return ticketSequence;
    }
}
