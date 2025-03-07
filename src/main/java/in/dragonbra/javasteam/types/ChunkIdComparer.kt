package `in`.dragonbra.javasteam.types

class ChunkIdComparer : AbstractSet<ByteArray>() {

    private val innerSet = HashSet<ByteArrayWrapper>()

    override val size: Int
        get() = innerSet.size

    override fun contains(element: ByteArray): Boolean = innerSet.contains(ByteArrayWrapper(element))

    override fun iterator(): Iterator<ByteArray> = innerSet.map { it.data }.iterator()

    fun add(element: ByteArray): Boolean = innerSet.add(ByteArrayWrapper(element))

    override fun isEmpty(): Boolean = innerSet.isEmpty()

    // Wrapper class to handle proper equals and hashCode for byte arrays
    private class ByteArrayWrapper(val data: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || other !is ByteArrayWrapper) return false
            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            // Similar to C# implementation - use first 4 bytes of the SHA-1 hash
            if (data.size >= 4) {
                return data[0].toInt() and 0xFF or
                    (data[1].toInt() and 0xFF shl 8) or
                    (data[2].toInt() and 0xFF shl 16) or
                    (data[3].toInt() and 0xFF shl 24)
            }
            return data.contentHashCode()
        }
    }
}
