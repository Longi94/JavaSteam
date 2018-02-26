package in.dragonbra.javasteam.steam.handlers.steamuser.callback;

import in.dragonbra.javasteam.enums.EAccountFlags;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverLogin.CMsgClientAccountInfo;
import in.dragonbra.javasteam.steam.steamclient.callbackmgr.CallbackMsg;

import java.util.EnumSet;

/**
 * This callback is received when account information is recieved from the network.
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

    public String getPersonaName() {
        return personaName;
    }

    public String getCountry() {
        return country;
    }

    public int getCountAuthedComputers() {
        return countAuthedComputers;
    }

    public EnumSet<EAccountFlags> getAccountFlags() {
        return accountFlags;
    }

    public long getFacebookID() {
        return facebookID;
    }

    public String getFacebookName() {
        return facebookName;
    }
}
