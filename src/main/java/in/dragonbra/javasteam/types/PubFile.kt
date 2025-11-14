package `in`.dragonbra.javasteam.types

/**
 * The base class used for wrapping common ULong types, to introduce type safety and distinguish between common types.
 */
@Suppress("unused")
abstract class UInt64Handle : Any {

    /**
     * Gets or sets the value.
     */
    protected open var value: ULong = 0UL

    /**
     * @constructor Initializes a new instance of the [UInt64Handle] class.
     */
    protected constructor()

    /**
     * Initializes a new instance of the [UInt64Handle] class.
     * @param value The value to initialize this handle to.
     */
    protected constructor(value: ULong) {
        this.value = value
    }

    /**
     * Returns a hash code for this instance.
     * @return A hash code for this instance, suitable for use in hashing algorithms and data structures like a hash table.
     */
    override fun hashCode(): Int = value.hashCode()

    /**
     * Determines whether the specified object is equal to this instance.
     * @param other The object to compare with this instance.
     * @return true if the specified object is equal to this instance; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        if (other is UInt64Handle) {
            return other.value == value
        }

        return false
    }

    /**
     * Returns a string that represents this instance.
     * @return A string that represents this instance.
     */
    override fun toString(): String = value.toString()

    /**
     * TODO
     */
    fun toLong(): Long = value.toLong()

    /**
     * Indicates whether the current object is equal to another object of the same type.
     * @param other An object to compare with this object.
     * @return true if the current object is equal to the other parameter; otherwise, false.
     */
    fun equals(other: UInt64Handle?): Boolean {
        if (other == null) {
            return false
        }
        return value == other.value
    }
}

/**
 * Represents a handle to a published file on the Steam workshop.
 */
class PublishedFileID : UInt64Handle {

    /**
     * Initializes a new instance of the PublishedFileID class.
     * @param fileId The file id.
     */
    constructor(fileId: Long = Long.MAX_VALUE) : super(fileId.toULong())

    companion object {
        /**
         * Implements the operator ==.
         * @param a The first published file.
         * @param b The second published file.
         * @return The result of the operator.
         */
        fun equals(a: PublishedFileID?, b: PublishedFileID?): Boolean {
            if (a === b) {
                return true
            }

            if (a == null || b == null) {
                return false
            }

            return a.value == b.value
        }

        /**
         * Implements the operator !=.
         * @param a The first published file.
         * @param b The second published file.
         * @return The result of the operator.
         */
        fun notEquals(a: PublishedFileID?, b: PublishedFileID?): Boolean = !equals(a, b)
    }
}
