package `in`.dragonbra.javasteam.types

/**
 * Represents a single unique handle to a piece of User Generated Content.
 */
@Suppress("unused")
class UGCHandle : GlobalID {

    /**
     * Initializes a new instance of the [UGCHandle] class.
     */
    constructor() : super()

    /**
     * Initializes a new instance of the [UGCHandle] class.
     * @param ugcId The UGC ID.
     */
    constructor(ugcId: Long) : super(ugcId)
}
