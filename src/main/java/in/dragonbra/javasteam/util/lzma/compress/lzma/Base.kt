package `in`.dragonbra.javasteam.util.lzma.compress.lzma

internal abstract class Base {
    companion object {
        const val kNumRepDistances = 4
        const val kNumStates = 12

        // val kLiteralNextStates = byteArrayOf(0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 4, 5)
        // val kMatchNextStates = byteArrayOf(7, 7, 7, 7, 7, 7, 7, 10, 10, 10, 10, 10)
        // val kRepNextStates = byteArrayOf(8, 8, 8, 8, 8, 8, 8, 11, 11, 11, 11, 11)
        // val kShortRepNextStates = byteArrayOf(9, 9, 9, 9, 9, 9, 9, 11, 11, 11, 11, 11)

        const val kNumPosSlotBits = 6
        const val kDicLogSizeMin = 0
        // const val kDicLogSizeMax = 30
        // const val kDistTableSizeMax = kDicLogSizeMax * 2

        const val kNumLenToPosStatesBits = 2 // it's for speed optimization
        const val kNumLenToPosStates = 1 shl kNumLenToPosStatesBits

        const val kMatchMinLen = 2

        fun getLenToPosState(len: Int): Int {
            val adjustedLen = len - kMatchMinLen
            return if (adjustedLen < kNumLenToPosStates) adjustedLen else kNumLenToPosStates - 1
        }

        const val kNumAlignBits = 4
        const val kAlignTableSize = 1 shl kNumAlignBits
        const val kAlignMask = kAlignTableSize - 1

        const val kStartPosModelIndex = 4
        const val kEndPosModelIndex = 14
        const val kNumPosModels = kEndPosModelIndex - kStartPosModelIndex

        const val kNumFullDistances = 1 shl (kEndPosModelIndex / 2)

        const val kNumLitPosStatesBitsEncodingMax = 4
        const val kNumLitContextBitsMax = 8

        const val kNumPosStatesBitsMax = 4
        const val kNumPosStatesMax = 1 shl kNumPosStatesBitsMax
        const val kNumPosStatesBitsEncodingMax = 4
        const val kNumPosStatesEncodingMax = 1 shl kNumPosStatesBitsEncodingMax

        const val kNumLowLenBits = 3
        const val kNumMidLenBits = 3
        const val kNumHighLenBits = 8
        const val kNumLowLenSymbols = 1 shl kNumLowLenBits
        const val kNumMidLenSymbols = 1 shl kNumMidLenBits
        const val kNumLenSymbols = kNumLowLenSymbols + kNumMidLenSymbols +
                (1 shl kNumHighLenBits)
        const val kMatchMaxLen = kMatchMinLen + kNumLenSymbols - 1
    }
}
