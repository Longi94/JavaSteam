package `in`.dragonbra.javasteam.depotdownloader

/**
 * Reuses the byte arrays a chunk passes through (downloaded, then decompressed), so a download
 * does not allocate two fresh arrays for every chunk.
 *
 * Every pooled array has the same size, [BUFFER_SIZE], so any of them fits either use. A rented
 * array is usually longer than the data in it: callers must carry the length of the data
 * themselves and never use the array's size.
 *
 * A rented array that is never given back is harmless, it is simply garbage collected. An array
 * must not be given back twice, or while anything still reads or writes it.
 *
 * @param maxIdle The most arrays kept while not in use. Arrays given back beyond that are dropped.
 */
internal class ChunkBufferPool(maxIdle: Int) {

    companion object {
        /**
         * Depot chunks are at most 1 MiB uncompressed. The slack is for a chunk that does not
         * compress: its downloaded form is slightly longer than its content (container header and
         * footer, the IV and the cipher padding).
         */
        const val BUFFER_SIZE: Int = (1024 + 64) * 1024
    }

    private val maxIdle: Int = maxIdle.coerceAtLeast(0)

    private val idle = ArrayDeque<ByteArray>()

    /** Number of arrays currently kept for reuse. */
    val idleCount: Int get() = synchronized(idle) { idle.size }

    /**
     * Returns an array of at least [minSize] bytes. Its contents are whatever its last user left in it.
     * A request above [BUFFER_SIZE] gets an array of its own, which is not kept when given back.
     */
    fun rent(minSize: Int): ByteArray {
        if (minSize > BUFFER_SIZE) {
            return ByteArray(minSize)
        }

        return synchronized(idle) { idle.removeLastOrNull() } ?: ByteArray(BUFFER_SIZE)
    }

    /** Hands [array] back for reuse. The caller must not touch it afterwards. */
    fun giveBack(array: ByteArray) {
        if (array.size != BUFFER_SIZE) {
            return
        }

        synchronized(idle) {
            if (idle.size < maxIdle) {
                idle.addLast(array)
            }
        }
    }

    /** Drops every idle array. */
    fun clear() {
        synchronized(idle) { idle.clear() }
    }
}
