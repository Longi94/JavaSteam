package `in`.dragonbra.javasteam.util.lzma.common

class CRC {
    companion object {
        val table: IntArray = IntArray(256)

        init {
            val kPoly = 0xEDB88320.toInt()
            for (i in 0 until 256) {
                var r = i
                for (j in 0 until 8) {
                    if ((r and 1) != 0) {
                        r = (r ushr 1) xor kPoly
                    } else {
                        r = r ushr 1
                    }
                }
                table[i] = r
            }
        }

        fun calculateDigest(data: ByteArray, offset: Int, size: Int): Int {
            val crc = CRC()
            crc.update(data, offset, size)
            return crc.getDigest()
        }

        fun verifyDigest(digest: Int, data: ByteArray, offset: Int, size: Int): Boolean {
            return calculateDigest(data, offset, size) == digest
        }
    }

    private var value: Int = -1

    fun init() {
        value = -1
    }

    fun updateByte(b: Byte) {
        value = table[(value.toByte().toInt() xor b.toInt()) and 0xFF] xor (value ushr 8)
    }

    fun update(data: ByteArray, offset: Int, size: Int) {
        for (i in 0 until size) {
            value = table[(value.toByte().toInt() xor data[offset + i].toInt()) and 0xFF] xor (value ushr 8)
        }
    }

    fun getDigest(): Int = value xor -1
}
