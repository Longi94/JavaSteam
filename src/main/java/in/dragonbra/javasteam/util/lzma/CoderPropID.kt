package `in`.dragonbra.javasteam.util.lzma

/**
 * Provides the fields that represent properties identifiers for compressing.
 */
enum class CoderPropID {
    /**
     * Specifies default property.
     */
    DefaultProp,

    /**
     * Specifies size of dictionary.
     */
    DictionarySize,

    /**
     * Specifies size of memory for PPM*.
     */
    UsedMemorySize,

    /**
     * Specifies order for PPM methods.
     */
    Order,

    /**
     * Specifies Block Size.
     */
    BlockSize,

    /**
     * Specifies number of position state bits for LZMA (0 <= x <= 4).
     */
    PosStateBits,

    /**
     * Specifies number of literal context bits for LZMA (0 <= x <= 8).
     */
    LitContextBits,

    /**
     * Specifies number of literal position bits for LZMA (0 <= x <= 4).
     */
    LitPosBits,

    /**
     * Specifies number of fast bytes for LZ*.
     */
    NumFastBytes,

    /**
     * Specifies match finder. LZMA: "BT2", "BT4" or "BT4B".
     */
    MatchFinder,

    /**
     * Specifies the number of match finder cycles.
     */
    MatchFinderCycles,

    /**
     * Specifies number of passes.
     */
    NumPasses,

    /**
     * Specifies number of algorithm.
     */
    Algorithm,

    /**
     * Specifies the number of threads.
     */
    NumThreads,

    /**
     * Specifies mode with end marker.
     */
    EndMarker
}
