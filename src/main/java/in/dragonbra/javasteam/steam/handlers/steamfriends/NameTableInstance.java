package in.dragonbra.javasteam.steam.handlers.steamfriends;

import in.dragonbra.javasteam.enums.EResult;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserver.CMsgClientAMGetPersonaNameHistoryResponse;
import in.dragonbra.javasteam.types.SteamID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a name table of an account.
 */
public class NameTableInstance {

    private final EResult result;

    private final SteamID steamID;

    private List<NameInstance> names;

    public NameTableInstance(CMsgClientAMGetPersonaNameHistoryResponse.NameTableInstance instance) {
        result = EResult.from(instance.getEresult());
        steamID = new SteamID(instance.getSteamid());

        names = new ArrayList<>();

        for (CMsgClientAMGetPersonaNameHistoryResponse.NameTableInstance.NameInstance nameInstance : instance.getNamesList()) {
            names.add(new NameInstance(nameInstance));
        }

        names = Collections.unmodifiableList(names);
    }

    /**
     * @return the result of querying this name table
     */
    public EResult getResult() {
        return result;
    }

    /**
     * @return the steam id this name table belongs to
     */
    public SteamID getSteamID() {
        return steamID;
    }

    /**
     * @return the names in this name table
     */
    public List<NameInstance> getNames() {
        return names;
    }
}
