@file:Suppress("unused")

package `in`.dragonbra.javasteam.steam.handlers.steammatchmaking

import `in`.dragonbra.javasteam.enums.ELobbyComparison
import `in`.dragonbra.javasteam.enums.ELobbyDistanceFilter
import `in`.dragonbra.javasteam.enums.ELobbyFilterType
import `in`.dragonbra.javasteam.protobufs.steamclient.SteammessagesClientserverMms

/**
 * The lobby filter base class.
 *
 * @constructor Base constructor for all filter subclasses.
 * @param filterType The type of filter.
 * @param key The metadata key this filter pertains to.
 * @param comparison The comparison method used by this filter.
 *
 * @property filterType The type of filter.
 * @property key The metadata key this filter pertains to. Under certain circumstances e.g. a distance filter, this will be an empty string.
 * @property comparison The comparison method used by this filter.
 *
 * @author Lossy
 * @since 2025-05-21
 */
abstract class Filter(
    val filterType: ELobbyFilterType,
    val key: String,
    val comparison: ELobbyComparison,
) {
    /**
     * Serializes the filter into a representation used internally by SteamMatchmaking.
     * @return A protobuf serializable representation of this filter.
     */
    open fun serialize(): SteammessagesClientserverMms.CMsgClientMMSGetLobbyList.Filter.Builder =
        SteammessagesClientserverMms.CMsgClientMMSGetLobbyList.Filter.newBuilder().apply {
            this.filterType = this@Filter.filterType.code()
            this.key = this@Filter.key
            this.comparision = this@Filter.comparison.code()
        }
}

/**
 * Can be used to filter lobbies geographically (based on IP according to Steam's IP database).
 *
 * @constructor Initializes a new instance of the [DistanceFilter] class.
 * @param value Steam distance filter value.
 *
 * @property value Steam distance filter value.
 *
 * @author Lossy
 * @since 2025-05-21
 */
class DistanceFilter(
    val value: ELobbyDistanceFilter,
) : Filter(
    filterType = ELobbyFilterType.Distance,
    key = "",
    comparison = ELobbyComparison.Equal
) {
    /**
     * Serializes the distance filter into a representation used internally by SteamMatchmaking.
     * @return A protobuf serializable representation of this filter.
     */
    override fun serialize(): SteammessagesClientserverMms.CMsgClientMMSGetLobbyList.Filter.Builder =
        super.serialize().apply {
            this.value = this@DistanceFilter.value.code().toString()
        }
}

/**
 * Can be used to filter lobbies with a metadata value closest to the specified value. Multiple
 * near filters can be specified, with former filters taking precedence over latter filters.
 *
 * @constructor Initializes a new instance of the [NearValueFilter] class.
 * @param key The metadata key this filter pertains to.
 * @param value Integer value to compare against
 *
 * @param value Integer value that lobbies' metadata value should be close to.
 *
 * @author Lossy
 * @since 2025-05-21
 */
class NearValueFilter(
    key: String,
    val value: Int,
) : Filter(
    filterType = ELobbyFilterType.NearValue,
    key = key,
    comparison = ELobbyComparison.Equal
) {
    /**
     * Serializes the slots available filter into a representation used internally by SteamMatchmaking.
     * @return A protobuf serializable representation of this filter.
     */
    override fun serialize(): SteammessagesClientserverMms.CMsgClientMMSGetLobbyList.Filter.Builder =
        super.serialize().apply {
            this.value = this@NearValueFilter.value.toString()
        }
}

/**
 * Can be used to filter lobbies by comparing an integer against a value in each lobby's metadata.
 *
 * @constructor Initializes a new instance of the [NumericalFilter] class.
 * @param key The metadata key this filter pertains to.
 * @param comparison The comparison method used by this filter.
 * @param value Integer value to compare against.
 *
 * @property value Integer value to compare against.
 *
 * @author Lossy
 * @since 2025-05-21
 */
class NumericalFilter(
    key: String,
    val value: Int,
    comparison: ELobbyComparison,
) : Filter(
    filterType = ELobbyFilterType.Numerical,
    key = key,
    comparison = comparison
) {
    /**
     * Serializes the numerical filter into a representation used internally by SteamMatchmaking.
     * @return A protobuf serializable representation of this filter.
     */
    override fun serialize(): SteammessagesClientserverMms.CMsgClientMMSGetLobbyList.Filter.Builder =
        super.serialize().apply {
            this.value = this@NumericalFilter.value.toString()
        }
}

/**
 * Can be used to filter lobbies by minimum number of slots available.
 *
 * @constructor Initializes a new instance of the [SlotsAvailableFilter] class.
 * @param slotsAvailable Integer value to compare against.
 *
 * @property slotsAvailable Minimum number of slots available in the lobby.
 *
 * @author Lossy
 * @since 2025-05-21
 */
class SlotsAvailableFilter(
    val slotsAvailable: Int,
) : Filter(
    filterType = ELobbyFilterType.SlotsAvailable,
    key = "",
    comparison = ELobbyComparison.Equal,
) {
    /**
     * Serializes the slots available filter into a representation used internally by SteamMatchmaking.
     * @return A protobuf serializable representation of this filter.
     */
    override fun serialize(): SteammessagesClientserverMms.CMsgClientMMSGetLobbyList.Filter.Builder =
        super.serialize().apply {
            this.value = slotsAvailable.toString()
        }
}

/**
 * Can be used to filter lobbies by comparing a string against a value in each lobby's metadata.
 *
 * @constructor Initializes a new instance of the [StringFilter] class.
 * @param key The metadata key this filter pertains to.
 * @param comparison The comparison method used by this filter.
 * @param value String value to compare against.
 *
 * @property value String value to compare against.
 *
 * @author Lossy
 * @since 2025-05-21
 */
class StringFilter(
    key: String,
    val value: String,
    comparison: ELobbyComparison,
) : Filter(
    filterType = ELobbyFilterType.String,
    key = key,
    comparison = comparison
) {
    /**
     * Serializes the string filter into a representation used internally by SteamMatchmaking.
     * @return A protobuf serializable representation of this filter.
     */
    override fun serialize(): SteammessagesClientserverMms.CMsgClientMMSGetLobbyList.Filter.Builder =
        super.serialize().apply {
            this.value = this@StringFilter.value
        }
}
