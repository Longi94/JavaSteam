package in.dragonbra.javasteam.types;

import in.dragonbra.javasteam.util.compat.ObjectsCompat;

import java.util.Date;

/**
 * Represents a globally unique identifier within the Steam network.
 * Guaranteed to be unique across all racks and servers for a given Steam universe.
 */
public class GlobalID {

    private final BitVector64 gidBits;

    /**
     * Initializes a new instance of the {@link GlobalID} class.
     */
    public GlobalID() {
        this(0xFFFFFFFFFFFFFFFFL);
    }

    /**
     * Initializes a new instance of the {@link GlobalID} class.
     *
     * @param gid The GID value.
     */
    public GlobalID(long gid) {
        gidBits = new BitVector64(gid);
    }

    /**
     * Sets the sequential count for this GID.
     *
     * @param value The sequential count.
     */
    public void setSequentialCount(long value) {
        gidBits.setMask((short) 0, 0xFFFFFL, value);
    }

    /**
     * Gets the sequential count for this GID.
     *
     * @return The sequential count.
     */
    public long getSequentialCount() {
        return gidBits.getMask((short) 0, 0xFFFFFL);
    }

    /**
     * Sets the start time of the server that generated this GID.
     *
     * @param startTime The start time.
     */
    public void setStartTime(Date startTime) {
        long startTimeS = (startTime.getTime() - 1104537600000L) / 1000L;
        gidBits.setMask((short) 20, 0x3FFFFFFFL, startTimeS);
    }

    /**
     * Gets the start time of the server that generated this GID.
     *
     * @return The start time.
     */
    public Date getStartTime() {
        long startTimeS = gidBits.getMask((short) 20, 0x3FFFFFFFL);
        return new Date(startTimeS * 1000L);
    }

    /**
     * Sets the process ID of the server that generated this GID.
     *
     * @param value The process ID.
     */
    public void setProcessID(long value) {
        gidBits.setMask((short) 50, 0xFL, value);
    }

    /**
     * Gets the process ID of the server that generated this GID.
     *
     * @return The process ID.
     */
    public long getProcessID() {
        return gidBits.getMask((short) 50, 0xFL);
    }

    /**
     * Sets the box ID of the server that generated this GID.
     *
     * @param value The box ID.
     */
    public void setBoxID(long value) {
        gidBits.setMask((short) 54, 0x3FFL, value);
    }

    /**
     * Gets the box ID of the server that generated this GID.
     *
     * @return The box ID.
     */
    public long getBoxID() {
        return gidBits.getMask((short) 54, 0x3FFL);
    }

    /**
     * Sets the entire 64bit value of this GID.
     *
     * @param value The value.
     */
    public void setValue(long value) {
        gidBits.setData(value);
    }

    /**
     * Sets the entire 64bit value of this GID.
     *
     * @return The value.
     */
    public long getValue() {
        return gidBits.getData();
    }

    /**
     * Determines whether the specified {@link Object} is equal to this instance.
     *
     * @param obj The {@link Object} to compare with this instance.
     * @return <b>true</b> if the specified {@link Object} is equal to this instance; otherwise, <b>false</b>.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof GlobalID)) {
            return false;
        }

        return ObjectsCompat.equals(gidBits.getData(), ((GlobalID) obj).gidBits.getData());
    }

    /**
     * Returns a hash code for this instance.
     *
     * @return A hash code for this instance, suitable for use in hashing algorithms and data structures like a hash table.
     */
    @Override
    public int hashCode() {
        return gidBits.getData().hashCode();
    }

    /**
     * Returns a {@link String} that represents this instance.
     *
     * @return A {@link String} that represents this instance.
     */
    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}
