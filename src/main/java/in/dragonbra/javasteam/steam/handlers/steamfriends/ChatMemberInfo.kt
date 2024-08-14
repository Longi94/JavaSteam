package `in`.dragonbra.javasteam.steam.handlers.steamfriends

import `in`.dragonbra.javasteam.enums.EChatPermission
import `in`.dragonbra.javasteam.enums.EClanPermission
import `in`.dragonbra.javasteam.types.KeyValue
import `in`.dragonbra.javasteam.types.MessageObject
import `in`.dragonbra.javasteam.types.SteamID
import java.util.*

/**
 * Represents the details of a user which is a member of a chatroom.
 */
@Suppress("unused")
class ChatMemberInfo : MessageObject {

    /**
     * Initializes a new instance of the [ChatMemberInfo] class.
     *
     * @param keyValues The KeyValue backing store for this member info.
     */
    constructor(keyValues: KeyValue?) : super(keyValues)

    /**
     * Initializes a new instance of the [ChatMemberInfo] class.
     */
    constructor() : super()

    /**
     * Gets the clan permission details of this chat member.
     */
    val details: EnumSet<EClanPermission>
        get() = keyValues.get("Details").asEnum(EClanPermission::class.java, EnumSet.of(EClanPermission.Nobody))

    /**
     * Gets the permissions this user has with the chatroom.
     */
    val permissions: EnumSet<EChatPermission>
        get() = keyValues.get("Details").asEnum(EChatPermission::class.java, EChatPermission.EveryoneDefault)

    /**
     * @return the [SteamID] of this user.
     */
    val steamID: SteamID
        get() = SteamID(keyValues.get("SteamID").asLong())
}
