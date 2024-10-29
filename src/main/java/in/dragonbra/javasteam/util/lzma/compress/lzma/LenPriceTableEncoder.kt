package `in`.dragonbra.javasteam.util.lzma.compress.lzma

import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.Encoder

class LenPriceTableEncoder : LenEncoder() {
    private val prices = IntArray(Base.kNumLenSymbols shl Base.kNumPosStatesBitsEncodingMax)
    private var tableSize: Int = 0
    private val counters = IntArray(Base.kNumPosStatesEncodingMax)

    fun setTableSize(tableSize: Int) {
        this.tableSize = tableSize
    }

    fun getPrice(symbol: Int, posState: Int): Int {
        return prices[posState * Base.kNumLenSymbols + symbol]
    }

    private fun updateTable(posState: Int) {
        setPrices(posState, tableSize, prices, posState * Base.kNumLenSymbols)
        counters[posState] = tableSize
    }

    fun updateTables(numPosStates: Int) {
        for (posState in 0 until numPosStates) {
            updateTable(posState)
        }
    }

    override fun encode(rangeEncoder: Encoder, symbol: Int, posState: Int) {
        super.encode(rangeEncoder, symbol, posState)
        if (--counters[posState] == 0) {
            updateTable(posState)
        }
    }
}
