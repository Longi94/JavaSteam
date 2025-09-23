package `in`.dragonbra.javasteam.types

/**
 * Represents an identifier of a network task known as a job.
 */
class JobID : GlobalID {

    companion object {
        /**
         * Represents an invalid JobID.
         */
        @JvmField
        val INVALID: JobID = JobID()
    }

    /**
     * Initializes a new instance of the [JobID] class.
     */
    constructor() : super()

    /**
     * Initializes a new instance of the [JobID] class.
     * @param gid The Job ID to initialize this instance with.
     */
    constructor(gid: Long) : super(gid)
}
