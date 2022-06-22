package in.dragonbra.javasteam.steam.handlers.steammatchmaking;

import in.dragonbra.javasteam.enums.ELobbyComparison;
import in.dragonbra.javasteam.enums.ELobbyDistanceFilter;
import in.dragonbra.javasteam.enums.ELobbyFilterType;
import in.dragonbra.javasteam.enums.ELobbyType;
import in.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms.CMsgClientMMSGetLobbyList;
import in.dragonbra.javasteam.types.KeyValue;
import in.dragonbra.javasteam.types.SteamID;
import in.dragonbra.javasteam.util.stream.MemoryStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Steam lobby.
 *
 * @author lossy
 * @since 2022-06-21
 */
public class Lobby {

    /**
     * The lobby filter base class.
     */
    abstract class Filter {

        /**
         * The type of filter.
         */
        private ELobbyFilterType filterType;

        /**
         * The metadata key this filter pertains to. Under certain circumstances e.g. a distance
         * filter, this will be an empty string.
         */
        private String key;

        /**
         * The comparison method used by this filter.
         */
        private ELobbyComparison comparison;

        /**
         * Base constructor for all filter sub-classes.
         *
         * @param filterType The type of filter.
         * @param key        The metadata key this filter pertains to.
         * @param comparison The comparison method used by this filter.
         */
        public Filter(ELobbyFilterType filterType, String key, ELobbyComparison comparison) {
            this.filterType = filterType;
            this.key = key;
            this.comparison = comparison;
        }

        /**
         * Serializes the filter into a representation used internally by SteamMatchmaking.
         * <p>
         * Note: Will need to be built to be anything meaningful 'serialize().build();'
         *
         * @return A protobuf serializable representation of this filter.
         */
        public CMsgClientMMSGetLobbyList.Filter.Builder serialize() {
            CMsgClientMMSGetLobbyList.Filter.Builder filter = CMsgClientMMSGetLobbyList.Filter.newBuilder();
            filter.setFilterType(filterType.code());
            filter.setKey(key);
            filter.setComparision(comparison.code());

            return filter;
        }

        public ELobbyFilterType getFilterType() {
            return filterType;
        }

        public String getKey() {
            return key;
        }

        public ELobbyComparison getComparison() {
            return comparison;
        }
    }

    /**
     * Can be used to filter lobbies geographically (based on IP according to Steam's IP database).
     */
    public class DistanceFilter extends Filter {

        /**
         * Steam distance filter value.
         */
        private ELobbyDistanceFilter value;


        /**
         * Initializes a new instance of the {@link DistanceFilter} class.
         *
         * @param value Steam distance filter value
         */
        public DistanceFilter(ELobbyDistanceFilter value) {
            super(ELobbyFilterType.Distance, "", ELobbyComparison.Equal);

            this.value = value;
        }

        /**
         * Serializes the distance filter into a representation used internally by SteamMatchmaking.
         *
         * @return A protobuf serializable representation of this filter.
         */
        @Override
        public CMsgClientMMSGetLobbyList.Filter.Builder serialize() {
            CMsgClientMMSGetLobbyList.Filter.Builder filter = super.serialize();
            filter.setValue(String.valueOf(value.code()));

            return filter;
        }

        public ELobbyDistanceFilter getValue() {
            return value;
        }
    }

    /**
     * Can be used to filter lobbies with a metadata value closest to the specified value. Multiple
     * near filters can be specified, with former filters taking precedence over latter filters.
     */
    public class NearValueFilter extends Filter {

        /**
         * Integer value that lobbies' metadata value should be close to.
         */
        private int value;

        /**
         * Initializes a new instance of the {@link  NearValueFilter}
         *
         * @param key   The metadata key this filter pertains to.
         * @param value Integer value to compare against.
         */
        public NearValueFilter(String key, int value) {
            super(ELobbyFilterType.NearValue, key, ELobbyComparison.Equal);

            this.value = value;
        }

        /**
         * Serializes the slots available filter into a representation used internally by SteamMatchmaking.
         *
         * @return A protobuf serializable representation of this filter.
         */
        @Override
        public CMsgClientMMSGetLobbyList.Filter.Builder serialize() {
            CMsgClientMMSGetLobbyList.Filter.Builder filter = super.serialize();
            filter.setValue(String.valueOf(value));

            return filter;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Can be used to filter lobbies by comparing an integer against a value in each lobby's metadata.
     */
    public class NumericalFilter extends Filter {

        /**
         * Integer value to compare against.
         */
        private int value;

        /**
         * Initializes a new instance of the {@link NumericalFilter} class.
         *
         * @param key        The metadata key this filter pertains to.
         * @param comparison The comparison method used by this filter.
         * @param value      Integer value to compare against.
         */
        public NumericalFilter(String key, ELobbyComparison comparison, int value) {
            super(ELobbyFilterType.Numerical, key, comparison);

            this.value = value;
        }

        /**
         * Serializes the numerical filter into a representation used internally by SteamMatchmaking.
         *
         * @return A protobuf serializable representation of this filter.
         */
        @Override
        public CMsgClientMMSGetLobbyList.Filter.Builder serialize() {
            CMsgClientMMSGetLobbyList.Filter.Builder filter = super.serialize();
            filter.setValue(String.valueOf(value));

            return filter;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Can be used to filter lobbies by minimum number of slots available.
     */
    public class SlotsAvailableFilter extends Filter {

        /**
         * Minumum number of slots available in the lobby.
         */
        private int slotsAvailable;

        /**
         * Initializes a new instance of the {@link  SlotsAvailableFilter} class.
         *
         * @param slotsAvailable Integer value to compare against.
         */
        public SlotsAvailableFilter(int slotsAvailable) {
            super(ELobbyFilterType.SlotsAvailable, "", ELobbyComparison.Equal);

            this.slotsAvailable = slotsAvailable;
        }


        /**
         * Serializes the slots available filter into a representation used internally by SteamMatchmaking.
         *
         * @return A protobuf serializable representation of this filter.
         */
        @Override
        public CMsgClientMMSGetLobbyList.Filter.Builder serialize() {
            CMsgClientMMSGetLobbyList.Filter.Builder filter = super.serialize();
            filter.setValue(String.valueOf(slotsAvailable));

            return filter;
        }

        public int getSlotsAvailable() {
            return slotsAvailable;
        }
    }

    /**
     * Can be used to filter lobbies by comparing a string against a value in each lobby's metadata.
     */
    public class StringFilter extends Filter {

        /**
         * String value to compare against.
         */
        private String value;

        /**
         * Initializes a new instance of the {@link  StringFilter} class.
         *
         * @param key        The metadata key this filter pertains to.
         * @param comparison The comparison method used by this filter.
         * @param value      String value to compare against.
         */
        private StringFilter(String key, ELobbyComparison comparison, String value) {
            super(ELobbyFilterType.String, key, comparison);

            this.value = value;
        }

        /**
         * Serializes the string filter into a representation used internally by SteamMatchmaking.
         *
         * @return A protobuf serializable representation of this filter.
         */
        @Override
        public CMsgClientMMSGetLobbyList.Filter.Builder serialize() {
            CMsgClientMMSGetLobbyList.Filter.Builder filter = super.serialize();
            filter.setValue(value);

            return filter;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Represents a Steam user within a lobby.
     */
    public static class Member {

        /**
         * SteamID of the lobby member.
         */
        private SteamID steamID;

        /**
         * Steam persona of the lobby member.
         */
        private String personaName;

        /**
         * Metadata attached to the lobby member.
         */
        public HashMap<String, String> metadata;

        public Member(long steamID, String personaName, HashMap<String, String> metadata) {
            this.steamID = new SteamID(steamID);
            this.personaName = personaName;
            this.metadata = metadata;
        }

        public Member(SteamID steamID, String personaName, HashMap<String, String> metadata) {
            this.steamID = steamID;
            this.personaName = personaName;
            this.metadata = metadata;
        }

        /**
         * Checks to see if this lobby member is equal to another. Only the SteamID of the lobby member is taken into account.
         *
         * @param obj ""
         * @return true, if obj is {@link  Member} with a matching SteamID. Otherwise, false.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Member) {
                return steamID.equals(((Member) obj).steamID);
            }

            return false;
        }

        /**
         * Hash code of the lobby member. Only the SteamID of the lobby member is taken into account.
         *
         * @return The hash code of this lobby member.
         */
        @Override
        public int hashCode() {
            return steamID.hashCode();
        }

        public SteamID getSteamID() {
            return steamID;
        }

        public String getPersonaName() {
            return personaName;
        }
    }

    /**
     * SteamID of the lobby.
     */
    private SteamID steamID;

    /**
     * The type of the lobby.
     */
    private ELobbyType lobbyType;

    /**
     * The lobby's flags.
     */
    private int lobbyFlags;

    /**
     * The SteamID of the lobby's owner. Please keep in mind that Steam does not provide lobby
     * owner details for lobbies returned in a lobby list. As such, lobbies that have been
     * obtained/updated as a result of calling {@link  SteamMatchmaking#getLobbyList}
     * may have a null (or non-null but state) owner.
     */
    private SteamID ownerSteamID;

    /**
     * The metadata of the lobby; string key-value pairs.
     */
    private HashMap<String, String> metadata;

    /**
     * The maximum number of members that can occupy the lobby.
     */
    private int maxMembers;

    /**
     * The number of members that are currently occupying the lobby.
     */
    private int numMembers;

    /**
     * The number of members that are currently occupying the lobby.
     */
    private List<Member> members;

    /**
     * The distance of the lobby.
     */
    private Float distance;

    /**
     * The weight of the lobby.
     */
    private Long weight;

    public Lobby(SteamID steamID, ELobbyType lobbyType, int lobbyFlags, SteamID ownerSteamID,
                 HashMap<String, String> metadata, int maxMembers, int numMembers, List<Member> members,
                 Float distance, Long weight) {

        this.steamID = steamID;
        this.lobbyType = lobbyType;
        this.lobbyFlags = lobbyFlags;
        this.ownerSteamID = ownerSteamID;

        if (metadata != null) {
            this.metadata = metadata;
        } else {
            this.metadata = new HashMap<>();
        }

        this.maxMembers = maxMembers;
        this.numMembers = numMembers;

        if (members != null) {
            this.members = members;
        } else {
            this.members = new ArrayList<>();
        }

        this.distance = distance;
        this.weight = weight;
    }

    static byte[] encodeMetadata(HashMap<String, String> metadata) {
        KeyValue keyValue = new KeyValue("");

        if (metadata != null) {
            for (Map.Entry<String, String> data : metadata.entrySet()) {
                keyValue.set(data.getKey(), new KeyValue(null, data.getValue()));
            }
        }

        MemoryStream ms = new MemoryStream();
        try {
            keyValue.saveToStream(ms.asOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return ms.toByteArray();
    }

    static HashMap<String, String> decodeMetadata(byte[] buffer) {
        if (buffer == null || buffer.length == 0) {
            return new HashMap<>();
        }

        KeyValue keyValue = new KeyValue();

        try (MemoryStream ms = new MemoryStream((buffer))) {
            if (!keyValue.tryReadAsBinary(ms)) {
                throw new NumberFormatException("Lobby metadata is of an unexpected format");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        HashMap<String, String> metadata = new HashMap<>();
        for (KeyValue value : keyValue.getChildren()) {
            if (value.getName() == null || value.getValue() == null) {
                continue;
            }

            metadata.put(value.getName(), value.getValue());
        }

        return metadata;
    }

    public SteamID getSteamID() {
        return steamID;
    }

    public ELobbyType getLobbyType() {
        return lobbyType;
    }

    public int getLobbyFlags() {
        return lobbyFlags;
    }

    public SteamID getOwnerSteamID() {
        return ownerSteamID;
    }

    public HashMap<String, String> getMetadata() {
        return metadata;
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public int getNumMembers() {
        return numMembers;
    }

    public List<Member> getMembers() {
        return members;
    }

    public float getDistance() {
        return distance;
    }

    public long getWeight() {
        return weight;
    }
}
