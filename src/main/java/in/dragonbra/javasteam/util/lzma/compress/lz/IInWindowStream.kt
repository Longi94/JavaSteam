package `in`.dragonbra.javasteam.util.lzma.compress.lz

interface IInWindowStream {
    fun setStream(stream: java.io.InputStream)
    fun init()
    fun releaseStream()
    fun getIndexByte(index: Int): Byte
    fun getMatchLen(index: Int, distance: Int, limit: Int): Int
    fun getNumAvailableBytes(): Int
}
