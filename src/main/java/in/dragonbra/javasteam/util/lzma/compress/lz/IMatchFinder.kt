package `in`.dragonbra.javasteam.util.lzma.compress.lz

interface IMatchFinder : IInWindowStream {
    fun create(historySize: Int, keepAddBufferBefore: Int,
               matchMaxLen: Int, keepAddBufferAfter: Int)
    fun getMatches(distances: IntArray): Int
    fun skip(num: Int)
}
