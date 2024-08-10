package in.dragonbra.javasteam.steam.handlers.steamfriends;

import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientAMGetPersonaNameHistoryResponse;

import java.util.Date;

/**
 * Represents a name in a name table
 */
public class NameInstance {

    private final String name;

    private final Date nameSince;

    public NameInstance(CMsgClientAMGetPersonaNameHistoryResponse.NameTableInstance.NameInstance instance) {
        name = instance.getName();
        nameSince = new Date(instance.getNameSince() * 1000L);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the time stamp this name was first used
     */
    public Date getNameSince() {
        return nameSince;
    }
}
