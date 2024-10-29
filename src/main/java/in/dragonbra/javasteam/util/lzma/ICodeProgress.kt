package `in`.dragonbra.javasteam.util.lzma

interface ICodeProgress {
    /**
     * Callback progress.
     *
     * @param inSize input size. -1 if unknown.
     * @param outSize output size. -1 if unknown.
     */
    fun setProgress(inSize: Long, outSize: Long)
}
