package `in`.dragonbra.javasteam.util.lzma

interface ICoder {
    /**
     * Codes streams.
     *
     * @param inStream input Stream.
     * @param outStream output Stream.
     * @param inSize input Size. -1 if unknown.
     * @param outSize output Size. -1 if unknown.
     * @param progress callback progress reference.
     * @throws DataErrorException if input stream is not valid
     */
    @Throws(DataErrorException::class)
    fun code(inStream: java.io.InputStream, outStream: java.io.OutputStream,
             inSize: Long, outSize: Long, progress: ICodeProgress?)
}
