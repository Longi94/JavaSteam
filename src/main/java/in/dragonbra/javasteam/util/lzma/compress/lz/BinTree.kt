package `in`.dragonbra.javasteam.util.lzma.compress.lz

import `in`.dragonbra.javasteam.util.lzma.common.CRC

class BinTree : InWindow(), IMatchFinder {
    private var cyclicBufferPos: Int = 0
    private var cyclicBufferSize: Int = 0
    private var matchMaxLen: Int = 0

    private lateinit var son: IntArray
    private lateinit var hash: IntArray

    private var cutValue: Int = 0xFF
    private var hashMask: Int = 0
    private var hashSizeSum: Int = 0

    private val hashArray: Boolean = true

    companion object {
        private const val kHash2Size: Int = 1 shl 10
        private const val kHash3Size: Int = 1 shl 16
        private const val kBT2HashSize: Int = 1 shl 16
        private const val kStartMaxLen: Int = 1
        private const val kHash3Offset: Int = kHash2Size
        private const val kEmptyHashValue: Int = 0
        private const val kMaxValForNormalize: Int = Int.MAX_VALUE

        private var kNumHashDirectBytes: Int = 0
        private var kMinMatchCheck: Int = 4
        private var kFixHashSize: Int = kHash2Size + kHash3Size
    }

    fun setType(numHashBytes: Int) {
        if (numHashBytes > 2) {
            kNumHashDirectBytes = 0
            kMinMatchCheck = 4
            kFixHashSize = kHash2Size + kHash3Size
        } else {
            kNumHashDirectBytes = 2
            kMinMatchCheck = 2 + 1
            kFixHashSize = 0
        }
    }

    override fun init() {
        super.init()
        for (i in 0 until hashSizeSum) {
            hash[i] = kEmptyHashValue
        }
        cyclicBufferPos = 0
        reduceOffsets(-1)
    }

    override fun movePos() {
        if (++cyclicBufferPos >= cyclicBufferSize) {
            cyclicBufferPos = 0
        }
        super.movePos()
        if (pos == kMaxValForNormalize) {
            normalize()
        }
    }

    override fun getIndexByte(index: Int): Byte = super.getIndexByte(index)

    override fun getMatchLen(index: Int, distance: Int, limit: Int): Int = super.getMatchLen(index, distance, limit)

    override fun getNumAvailableBytes(): Int = super.getNumAvailableBytes()

    override fun create(historySize: Int, keepAddBufferBefore: Int, matchMaxLen: Int, keepAddBufferAfter: Int) {
        if (historySize > kMaxValForNormalize - 256) {
            throw Exception()
        }
        cutValue = 16 + (matchMaxLen shr 1)

        val windowReservSize = (historySize + keepAddBufferBefore + matchMaxLen + keepAddBufferAfter) / 2 + 256

        super.create(historySize + keepAddBufferBefore, matchMaxLen + keepAddBufferAfter, windowReservSize)

        this.matchMaxLen = matchMaxLen

        val cyclicBufferSize = historySize + 1
        if (this.cyclicBufferSize != cyclicBufferSize) {
            son = IntArray(cyclicBufferSize * 2)
            this.cyclicBufferSize = cyclicBufferSize
        }

        var hs = kBT2HashSize

        if (hashArray) {
            hs = historySize - 1
            hs = hs or (hs shr 1)
            hs = hs or (hs shr 2)
            hs = hs or (hs shr 4)
            hs = hs or (hs shr 8)
            hs = hs shr 1
            hs = hs or 0xFFFF
            if (hs > (1 shl 24)) {
                hs = hs shr 1
            }
            hashMask = hs
            hs++
            hs += kFixHashSize
        }
        if (hs != hashSizeSum) {
            hash = IntArray(hs)
            hashSizeSum = hs
        }
    }

    override fun getMatches(distances: IntArray): Int {
        val lenLimit = if (pos + matchMaxLen <= streamPos) matchMaxLen else {
            val tempLimit = streamPos - pos
            if (tempLimit < kMinMatchCheck) {
                movePos()
                return 0
            }
            tempLimit
        }

        var offset = 0
        val matchMinPos = if (pos > cyclicBufferSize) pos - cyclicBufferSize else 0
        val cur = bufferOffset + pos
        var maxLen = kStartMaxLen // to avoid items for len < hashSize
        var hashValue = 0
        var hash2Value = 0
        var hash3Value = 0

        if (hashArray) {
            val temp = CRC.table[bufferBase!![cur].toInt() and 0xFF] xor (bufferBase!![cur + 1].toInt() and 0xFF)
            hash2Value = temp and (kHash2Size - 1)
            hash3Value = (temp xor ((bufferBase!![cur + 2].toInt() and 0xFF) shl 8)) and (kHash3Size - 1)
            hashValue = (temp xor ((CRC.table[bufferBase!![cur + 3].toInt() and 0xFF] and 0xFF) shl 5)) and hashMask
        } else {
            hashValue = (bufferBase!![cur].toInt() and 0xFF) xor ((bufferBase!![cur + 1].toInt() and 0xFF) shl 8)
        }

        var curMatch = hash[kFixHashSize + hashValue]
        if (hashArray) {
            var curMatch2 = hash[hash2Value]
            val curMatch3 = hash[kHash3Offset + hash3Value]
            hash[hash2Value] = pos
            hash[kHash3Offset + hash3Value] = pos
            if (curMatch2 > matchMinPos) {
                if (bufferBase!![bufferOffset + curMatch2] == bufferBase!![cur]) {
                    distances[offset++] = maxLen
                    distances[offset++] = pos - curMatch2 - 1
                    maxLen = 2
                }
            }
            if (curMatch3 > matchMinPos) {
                if (bufferBase!![bufferOffset + curMatch3] == bufferBase!![cur]) {
                    if (curMatch3 == curMatch2) {
                        offset -= 2
                    }
                    distances[offset++] = maxLen
                    distances[offset++] = pos - curMatch3 - 1
                    maxLen = 3
                    curMatch2 = curMatch3
                }
            }
            if (offset != 0 && curMatch2 == curMatch) {
                offset -= 2
                maxLen = kStartMaxLen
            }
        }

        hash[kFixHashSize + hashValue] = pos

        var ptr0 = (cyclicBufferPos shl 1) + 1
        var ptr1 = (cyclicBufferPos shl 1)

        var len0 = kNumHashDirectBytes
        var len1 = kNumHashDirectBytes

        if (kNumHashDirectBytes != 0) {
            if (curMatch > matchMinPos) {
                if (bufferBase!![bufferOffset + curMatch + kNumHashDirectBytes] !=
                    bufferBase!![cur + kNumHashDirectBytes]
                ) {
                    distances[offset++] = maxLen
                    distances[offset++] = pos - curMatch - 1
                    maxLen = kNumHashDirectBytes
                }
            }
        }

        var count = cutValue

        while (true) {
            if (curMatch <= matchMinPos || count-- == 0) {
                son[ptr0] = kEmptyHashValue
                son[ptr1] = kEmptyHashValue
                break
            }
            val delta = pos - curMatch
            val cyclicPos = ((if (delta <= cyclicBufferPos) cyclicBufferPos - delta
            else cyclicBufferPos - delta + cyclicBufferSize) shl 1)

            val pby1 = bufferOffset + curMatch
            var len = minOf(len0, len1)
            if (bufferBase!![pby1 + len] == bufferBase!![cur + len]) {
                while (++len != lenLimit) {
                    if (bufferBase!![pby1 + len] != bufferBase!![cur + len]) {
                        break
                    }
                }
                if (maxLen < len) {
                    distances[offset++] = maxLen
                    distances[offset++] = delta - 1
                    maxLen = len
                    if (len == lenLimit) {
                        son[ptr1] = son[cyclicPos]
                        son[ptr0] = son[cyclicPos + 1]
                        break
                    }
                }
            }
            if ((bufferBase!![pby1 + len].toInt() and 0xFF) < (bufferBase!![cur + len].toInt() and 0xFF)) {
                son[ptr1] = curMatch
                ptr1 = cyclicPos + 1
                curMatch = son[ptr1]
                len1 = len
            } else {
                son[ptr0] = curMatch
                ptr0 = cyclicPos
                curMatch = son[ptr0]
                len0 = len
            }
        }
        movePos()
        return offset
    }

    override fun skip(num: Int) {
        var numRemaining = num
        do {
            val lenLimit = if (pos + matchMaxLen <= streamPos) matchMaxLen else {
                val tempLimit = streamPos - pos
                if (tempLimit < kMinMatchCheck) {
                    movePos()
                    continue
                }
                tempLimit
            }

            val matchMinPos = if (pos > cyclicBufferSize) pos - cyclicBufferSize else 0
            val cur = bufferOffset + pos

            var hashValue: Int

            if (hashArray) {
                var temp = CRC.table[bufferBase!![cur].toInt() and 0xFF] xor (bufferBase!![cur + 1].toInt() and 0xFF)
                val hash2Value = temp and (kHash2Size - 1)
                hash[hash2Value] = pos
                temp = temp xor ((bufferBase!![cur + 2].toInt() and 0xFF) shl 8)
                val hash3Value = temp and (kHash3Size - 1)
                hash[kHash3Offset + hash3Value] = pos
                hashValue = (temp xor ((CRC.table[bufferBase!![cur + 3].toInt() and 0xFF] and 0xFF) shl 5)) and hashMask
            } else {
                hashValue = (bufferBase!![cur].toInt() and 0xFF) xor ((bufferBase!![cur + 1].toInt() and 0xFF) shl 8)
            }

            var curMatch = hash[kFixHashSize + hashValue]
            hash[kFixHashSize + hashValue] = pos

            var ptr0 = (cyclicBufferPos shl 1) + 1
            var ptr1 = (cyclicBufferPos shl 1)

            var len0 = kNumHashDirectBytes
            var len1 = kNumHashDirectBytes

            var count = cutValue
            while (true) {
                if (curMatch <= matchMinPos || count-- == 0) {
                    son[ptr0] = kEmptyHashValue
                    son[ptr1] = kEmptyHashValue
                    break
                }

                val delta = pos - curMatch
                val cyclicPos = ((if (delta <= cyclicBufferPos) cyclicBufferPos - delta
                else cyclicBufferPos - delta + cyclicBufferSize) shl 1)

                val pby1 = bufferOffset + curMatch
                var len = minOf(len0, len1)
                if (bufferBase!![pby1 + len] == bufferBase!![cur + len]) {
                    while (++len != lenLimit) {
                        if (bufferBase!![pby1 + len] != bufferBase!![cur + len]) {
                            break
                        }
                    }
                    if (len == lenLimit) {
                        son[ptr1] = son[cyclicPos]
                        son[ptr0] = son[cyclicPos + 1]
                        break
                    }
                }
                if ((bufferBase!![pby1 + len].toInt() and 0xFF) < (bufferBase!![cur + len].toInt() and 0xFF)) {
                    son[ptr1] = curMatch
                    ptr1 = cyclicPos + 1
                    curMatch = son[ptr1]
                    len1 = len
                } else {
                    son[ptr0] = curMatch
                    ptr0 = cyclicPos
                    curMatch = son[ptr0]
                    len0 = len
                }
            }
            movePos()
        } while (--numRemaining != 0)
    }

    private fun normalizeLinks(items: IntArray, numItems: Int, subValue: Int) {
        for (i in 0 until numItems) {
            var value = items[i]
            if (value <= subValue) {
                value = kEmptyHashValue
            } else {
                value -= subValue
            }
            items[i] = value
        }
    }

    private fun normalize() {
        val subValue = pos - cyclicBufferSize
        normalizeLinks(son, cyclicBufferSize * 2, subValue)
        normalizeLinks(hash, hashSizeSum, subValue)
        reduceOffsets(subValue)
    }

    fun setCutValue(cutValue: Int) {
        this.cutValue = cutValue
    }
}
