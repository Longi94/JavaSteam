package `in`.dragonbra.javasteam.steam.handlers.steamcloud

/**
 * The type and pixel size of an image or video, read from its header.
 * Steam wants both when media is uploaded to a chat.
 *
 * @param mimeType The MIME type, like `image/png`.
 * @param width The width in pixels, zero for a video.
 * @param height The height in pixels, zero for a video.
 */
internal data class ChatMediaInfo(val mimeType: String, val width: Int, val height: Int) {

    /**
     * The file extension that goes with [mimeType], without a dot.
     */
    val extension: String
        get() = mimeType.substringAfter('/')

    companion object {
        private val PNG_SIGNATURE = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A)

        // The EBML header that WebM files start with
        private val WEBM_SIGNATURE = byteArrayOf(0x1A, 0x45, 0xDF.toByte(), 0xA3.toByte())

        // A pack header (program stream) or a sequence header (bare video stream)
        private val MPEG_SIGNATURES = listOf(
            byteArrayOf(0x00, 0x00, 0x01, 0xBA.toByte()),
            byteArrayOf(0x00, 0x00, 0x01, 0xB3.toByte()),
        )

        /**
         * Reads the type of a PNG, JPEG or GIF image, or a WebM, MPEG or OGV video.
         * The size is read for images only, Steam takes videos with a size of zero.
         *
         * @throws IllegalArgumentException if the data is not one of those, or its header is cut short.
         */
        fun detect(data: ByteArray): ChatMediaInfo = when {
            data.startsWith(PNG_SIGNATURE) -> png(data)

            data.startsWith("GIF87a") || data.startsWith("GIF89a") -> gif(data)

            data.size >= 2 && data.u8(0) == 0xFF && data.u8(1) == 0xD8 -> jpeg(data)

            data.startsWith(WEBM_SIGNATURE) -> ChatMediaInfo("video/webm", 0, 0)

            MPEG_SIGNATURES.any { data.startsWith(it) } -> ChatMediaInfo("video/mpeg", 0, 0)

            data.startsWith("OggS") -> ChatMediaInfo("video/ogv", 0, 0)

            else -> throw IllegalArgumentException(
                "Unsupported media format, expected a PNG, JPEG or GIF image, or a WebM, MPEG or OGV video"
            )
        }

        // The IHDR chunk always comes first, with the size as two big endian ints
        private fun png(data: ByteArray): ChatMediaInfo {
            require(data.size >= 24) { "Malformed PNG, the header is cut short" }
            return ChatMediaInfo("image/png", data.u32be(16), data.u32be(20))
        }

        // The logical screen size follows the signature as two little endian shorts
        private fun gif(data: ByteArray): ChatMediaInfo {
            require(data.size >= 10) { "Malformed GIF, the header is cut short" }
            return ChatMediaInfo("image/gif", data.u16le(6), data.u16le(8))
        }

        // The size is in the start of frame segment, which can sit behind any number of other segments
        private fun jpeg(data: ByteArray): ChatMediaInfo {
            var offset = 2

            while (offset + 4 <= data.size) {
                if (data.u8(offset) != 0xFF) {
                    break
                }

                val marker = data.u8(offset + 1)

                when {
                    // Padding before a marker
                    marker == 0xFF -> offset++

                    // Standalone markers, they have no length
                    marker == 0x01 || marker in 0xD0..0xD8 -> offset += 2

                    // Start of frame. C4, C8 and CC share the range but are other things.
                    marker in 0xC0..0xCF && marker != 0xC4 && marker != 0xC8 && marker != 0xCC -> {
                        if (offset + 9 > data.size) {
                            break
                        }
                        return ChatMediaInfo("image/jpeg", data.u16be(offset + 7), data.u16be(offset + 5))
                    }

                    else -> offset += 2 + data.u16be(offset + 2)
                }
            }

            throw IllegalArgumentException("Malformed JPEG, no frame header found")
        }

        private fun ByteArray.startsWith(prefix: ByteArray): Boolean =
            size >= prefix.size && prefix.indices.all { this[it] == prefix[it] }

        private fun ByteArray.startsWith(prefix: String): Boolean = startsWith(prefix.toByteArray(Charsets.US_ASCII))

        private fun ByteArray.u8(index: Int): Int = this[index].toInt() and 0xFF

        private fun ByteArray.u16be(index: Int): Int = (u8(index) shl 8) or u8(index + 1)

        private fun ByteArray.u16le(index: Int): Int = u8(index) or (u8(index + 1) shl 8)

        private fun ByteArray.u32be(index: Int): Int = (u16be(index) shl 16) or u16be(index + 2)
    }
}
