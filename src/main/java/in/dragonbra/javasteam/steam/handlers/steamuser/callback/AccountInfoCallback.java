package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.enums.EAccountFlags;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientAccountInfo;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

import java.util.EnumSet;

/**
 * This callback is received when account information is received from the network.
 * This generally happens after logon.
 */
public class AccountInfoCallback extends CallbackMsg {

    private String personaName;

    private String country;

    private int countAuthedComputers;

    private EnumSet<EAccountFlags> accountFlags;

    private long facebookID;

    private String facebookName;

    public AccountInfoCallback(CMsgClientAccountInfo.Builder msg) {
        personaName = msg.getPersonaName();
        country = msg.getIpCountry();

        countAuthedComputers = msg.getCountAuthedComputers();

        accountFlags = EAccountFlags.from(msg.getAccountFlags());

        facebookID = msg.getFacebookId();
        facebookName = msg.getFacebookName();
    }

    /**
     * @return the last recorded persona name used by this account.
     */
    public String getPersonaName() {
        return personaName;
    }

    /**
     * @return the country this account is connected from.
     */
    public String getCountry() {
        return country;
    }

    /**
     * @return the count of SteamGuard authenticated computers.
     */
    public int getCountAuthedComputers() {
        return countAuthedComputers;
    }

    /**
     * @return the account flags for this account. See {@link EAccountFlags}.
     */
    public EnumSet<EAccountFlags> getAccountFlags() {
        return accountFlags;
    }

    /**
     * @return the facebook ID of this account if it is linked with facebook.
     */
    public long getFacebookID() {
        return facebookID;
    }

    /**
     * @return the facebook name if this account is linked with facebook.
     */
    public String getFacebookName() {
        return facebookName;
    }
}
