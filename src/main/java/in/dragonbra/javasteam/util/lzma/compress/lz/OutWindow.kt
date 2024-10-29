package `in`.dragonbra.javasteam.util.lzma.compress.lz

class OutWindow {
    private var _buffer: ByteArray? = null
    private var _pos: Int = 0
    private var _windowSize: Int = 0
    private var _streamPos: Int = 0
    private var _stream: java.io.OutputStream? = null

    var trainSize: Int = 0

    // Added by SteamKit to avoid allocating a byte array
    internal fun steamKitSetBuffer(buffer: ByteArray, windowSize: Int) {
        _buffer = buffer
        _windowSize = windowSize
        _pos = 0
        _streamPos = 0
    }

    fun create(windowSize: Int) {
        if (_windowSize != windowSize) {
            // System.gc()
            _buffer = ByteArray(windowSize)
        }
        _windowSize = windowSize
        _pos = 0
        _streamPos = 0
    }

    fun init(stream: java.io.OutputStream, solid: Boolean) {
        releaseStream()
        _stream = stream
        if (!solid) {
            _streamPos = 0
            _pos = 0
            trainSize = 0
        }
    }

    fun train(stream: java.io.InputStream): Boolean {
        val len = stream.available().toLong()
        val size = if (len < _windowSize) len.toInt() else _windowSize
        trainSize = size
        stream.skip(len - size)
        _streamPos = 0
        _pos = 0
        var remainingSize = size
        while (remainingSize > 0) {
            val curSize = minOf(_windowSize - _pos, remainingSize)
            val numReadBytes = stream.read(_buffer!!, _pos, curSize)
            if (numReadBytes == 0) return false
            remainingSize -= numReadBytes
            _pos += numReadBytes
            _streamPos += numReadBytes
            if (_pos == _windowSize) {
                _streamPos = 0
                _pos = 0
            }
        }
        return true
    }

    fun releaseStream() {
        flush()
        _stream = null
    }

    fun flush() {
        val size = _pos - _streamPos
        if (size == 0) return
        _stream?.write(_buffer!!, _streamPos, size)
        if (_pos >= _windowSize) _pos = 0
        _streamPos = _pos
    }

    fun copyBlock(distance: Int, len: Int) {
        var pos = _pos - distance - 1
        if (pos >= _windowSize) pos += _windowSize
        var remaining = len
        while (remaining > 0) {
            if (pos >= _windowSize) pos = 0
            _buffer!![_pos++] = _buffer!![pos++]
            if (_pos >= _windowSize) flush()
            remaining--
        }
    }

    fun putByte(b: Byte) {
        _buffer!![_pos++] = b
        if (_pos >= _windowSize) flush()
    }

    fun getByte(distance: Int): Byte {
        var pos = _pos - distance - 1
        if (pos >= _windowSize) pos += _windowSize
        return _buffer!![pos]
    }
}
