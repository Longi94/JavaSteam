package `in`.dragonbra.javasteam.util.lzma.compress.lzma

import `in`.dragonbra.javasteam.util.lzma.*
import `in`.dragonbra.javasteam.util.lzma.compress.lz.BinTree
import `in`.dragonbra.javasteam.util.lzma.compress.lz.IMatchFinder
import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.BitEncoder
import `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.BitTreeEncoder
import java.io.InputStream
import java.io.OutputStream

class Encoder : ICoder, ISetCoderProperties, IWriteCoderProperties {
    companion object {
        const val kIfinityPrice: Int = 0x0FFFFFFF

        private const val kPropSize = 5
        private val properties = ByteArray(kPropSize)

        val g_FastPos = ByteArray(1 shl 11)

        init {
            val kFastSlots: Byte = 22
            var c = 2
            g_FastPos[0] = 0
            g_FastPos[1] = 1
            for (slotFast in 2 until kFastSlots) {
                val k = (1 shl ((slotFast.toInt() shr 1) - 1))
                for (j in 0 until k) {
                    g_FastPos[c++] = slotFast.toByte()
                }
            }
        }

        fun getPosSlot(pos: Int): Int {
            return when {
                pos < (1 shl 11) -> g_FastPos[pos].toInt()
                pos < (1 shl 21) -> g_FastPos[pos shr 10].toInt() + 20
                else -> g_FastPos[pos shr 20].toInt() + 40
            }
        }

        fun getPosSlot2(pos: Int): Int {
            return when {
                pos < (1 shl 17) -> g_FastPos[pos shr 6].toInt() + 12
                pos < (1 shl 27) -> g_FastPos[pos shr 16].toInt() + 32
                else -> g_FastPos[pos shr 26].toInt() + 52
            }
        }
    }

    private val _state = State()
    private var _previousByte: Byte = 0
    private val _repDistances = IntArray(Base.kNumRepDistances)

    private fun baseInit() {
        _state.init()
        _previousByte = 0
        for (i in 0 until Base.kNumRepDistances) {
            _repDistances[i] = 0
        }
    }

    private val kDefaultDictionaryLogSize = 22
    private val kNumFastBytesDefault = 0x20

    private val kNumLenSpecSymbols = Base.kNumLowLenSymbols + Base.kNumMidLenSymbols

    private val kNumOpts = 1 shl 12

    private val _optimum = Array(kNumOpts) { Optimal() }
    private var _matchFinder: IMatchFinder? = null
    private val _rangeEncoder = `in`.dragonbra.javasteam.util.lzma.compress.rangecoder.Encoder()

    private val _isMatch = Array(Base.kNumStates shl Base.kNumPosStatesBitsMax) { BitEncoder() }
    private val _isRep = Array(Base.kNumStates) { BitEncoder() }
    private val _isRepG0 = Array(Base.kNumStates) { BitEncoder() }
    private val _isRepG1 = Array(Base.kNumStates) { BitEncoder() }
    private val _isRepG2 = Array(Base.kNumStates) { BitEncoder() }
    private val _isRep0Long = Array(Base.kNumStates shl Base.kNumPosStatesBitsMax) { BitEncoder() }

    private val _posSlotEncoder = Array(Base.kNumLenToPosStates) { BitTreeEncoder(Base.kNumPosSlotBits) }

    private val _posEncoders = Array(Base.kNumFullDistances - Base.kEndPosModelIndex) { BitEncoder() }
    private val _posAlignEncoder = BitTreeEncoder(Base.kNumAlignBits)

    private val _lenEncoder = LenPriceTableEncoder()
    private val _repMatchLenEncoder = LenPriceTableEncoder()

    private val _literalEncoder = LiteralEncoder()

    private val _matchDistances = IntArray(Base.kMatchMaxLen * 2 + 2)

    private var _numFastBytes = kNumFastBytesDefault
    private var _longestMatchLength = 0
    private var _numDistancePairs = 0

    private var _additionalOffset = 0

    private var _optimumEndIndex = 0
    private var _optimumCurrentIndex = 0

    private var _longestMatchWasFound = false

    private val _posSlotPrices = IntArray(1 shl (Base.kNumPosSlotBits + Base.kNumLenToPosStatesBits))
    private val _distancesPrices = IntArray(Base.kNumFullDistances shl Base.kNumLenToPosStatesBits)
    private val _alignPrices = IntArray(Base.kAlignTableSize)
    private var _alignPriceCount = 0

    private var _distTableSize = kDefaultDictionaryLogSize * 2

    private var _posStateBits = 2
    private var _posStateMask = 4 - 1
    private var _numLiteralPosStateBits = 0
    private var _numLiteralContextBits = 3

    private var _dictionarySize = (1 shl kDefaultDictionaryLogSize)
    private var _dictionarySizePrev = -1
    private var _numFastBytesPrev = -1

    private var nowPos64: Long = 0
    private var _finished = false
    private var _inStream: InputStream? = null

    private var _matchFinderType = EMatchFinderType.BT4
    private var _writeEndMark = false

    private var _needReleaseMFStream = false

    private fun create() {
        if (_matchFinder == null) {
            val bt = BinTree()
            val numHashBytes = if (_matchFinderType == EMatchFinderType.BT2) 2 else 4
            bt.setType(numHashBytes)
            _matchFinder = bt
        }

        _literalEncoder.create(_numLiteralPosStateBits, _numLiteralContextBits)

        if (_dictionarySize == _dictionarySizePrev && _numFastBytesPrev == _numFastBytes) {
            return
        }
        _matchFinder!!.create(_dictionarySize, kNumOpts, _numFastBytes, Base.kMatchMaxLen + 1)
        _dictionarySizePrev = _dictionarySize
        _numFastBytesPrev = _numFastBytes
    }

    init {
        for (i in 0 until kNumOpts) {
            _optimum[i] = Optimal()
        }
        for (i in 0 until Base.kNumLenToPosStates) {
            _posSlotEncoder[i] = BitTreeEncoder(Base.kNumPosSlotBits)
        }
    }

    private fun setWriteEndMarkerMode(writeEndMarker: Boolean) {
        _writeEndMark = writeEndMarker
    }

    private fun init() {
        baseInit()
        _rangeEncoder.init()

        for (i in 0 until Base.kNumStates) {
            for (j in 0..(_posStateMask)) {
                val complexState = (i shl Base.kNumPosStatesBitsMax) + j
                _isMatch[complexState].init()
                _isRep0Long[complexState].init()
            }
            _isRep[i].init()
            _isRepG0[i].init()
            _isRepG1[i].init()
            _isRepG2[i].init()
        }

        _literalEncoder.init()
        for (i in 0 until Base.kNumLenToPosStates) {
            _posSlotEncoder[i].init()
        }
        for (i in 0 until Base.kNumFullDistances - Base.kEndPosModelIndex) {
            _posEncoders[i].init()
        }

        _lenEncoder.init(1 shl _posStateBits)
        _repMatchLenEncoder.init(1 shl _posStateBits)

        _posAlignEncoder.init()

        _longestMatchWasFound = false
        _optimumEndIndex = 0
        _optimumCurrentIndex = 0
        _additionalOffset = 0
    }

    private fun readMatchDistances(): Pair<Int, Int> {
        var lenRes = 0
        val numDistancePairs = _matchFinder!!.getMatches(_matchDistances)
        if (numDistancePairs > 0) {
            lenRes = _matchDistances[numDistancePairs - 2]
            if (lenRes == _numFastBytes) {
                lenRes += _matchFinder!!.getMatchLen(lenRes - 1, _matchDistances[numDistancePairs - 1],
                    Base.kMatchMaxLen - lenRes)
            }
        }
        _additionalOffset++
        return Pair(lenRes, numDistancePairs)
    }

    private fun movePos(num: Int) {
        if (num > 0) {
            _matchFinder!!.skip(num)
            _additionalOffset += num
        }
    }

    private fun getRepLen1Price(state: State, posState: Int): Int {
        return _isRepG0[state.index].getPrice0() +
                _isRep0Long[(state.index shl Base.kNumPosStatesBitsMax) + posState].getPrice0()
    }

    private fun getPureRepPrice(repIndex: Int, state: State, posState: Int): Int {
        var price: Int
        if (repIndex == 0) {
            price = _isRepG0[state.index].getPrice0()
            price += _isRep0Long[(state.index shl Base.kNumPosStatesBitsMax) + posState].getPrice1()
        } else {
            price = _isRepG0[state.index].getPrice1()
            if (repIndex == 1) {
                price += _isRepG1[state.index].getPrice0()
            } else {
                price += _isRepG1[state.index].getPrice1()
                price += _isRepG2[state.index].getPrice(repIndex - 2)
            }
        }
        return price
    }

    private fun getRepPrice(repIndex: Int, len: Int, state: State, posState: Int): Int {
        val price = _repMatchLenEncoder.getPrice(len - Base.kMatchMinLen, posState)
        return price + getPureRepPrice(repIndex, state, posState)
    }

    private fun getPosLenPrice(pos: Int, len: Int, posState: Int): Int {
        val lenToPosState = Base.getLenToPosState(len)
        val price = if (pos < Base.kNumFullDistances) {
            _distancesPrices[(lenToPosState * Base.kNumFullDistances) + pos]
        } else {
            _posSlotPrices[(lenToPosState shl Base.kNumPosSlotBits) + getPosSlot2(pos)] +
                    _alignPrices[pos and Base.kAlignMask]
        }
        return price + _lenEncoder.getPrice(len - Base.kMatchMinLen, posState)
    }

    private fun backward(cur: Int): Pair<Int, Int> {
        _optimumEndIndex = cur
        var localCur = cur
        var posMem = _optimum[localCur].posPrev
        var backMem = _optimum[localCur].backPrev
        do {
            if (_optimum[localCur].prev1IsChar) {
                _optimum[posMem].makeAsChar()
                _optimum[posMem].posPrev = posMem - 1
                if (_optimum[localCur].prev2) {
                    _optimum[posMem - 1].prev1IsChar = false
                    _optimum[posMem - 1].posPrev = _optimum[localCur].posPrev2
                    _optimum[posMem - 1].backPrev = _optimum[localCur].backPrev2
                }
            }
            val posPrev = posMem
            val backCur = backMem
            backMem = _optimum[posPrev].backPrev
            posMem = _optimum[posPrev].posPrev
            _optimum[posPrev].backPrev = backCur
            _optimum[posPrev].posPrev = localCur
            localCur = posPrev
        } while (localCur > 0)
        val backRes = _optimum[0].backPrev
        _optimumCurrentIndex = _optimum[0].posPrev
        return Pair(backRes, _optimumCurrentIndex)
    }

    private val reps = IntArray(Base.kNumRepDistances)
    private val repLens = IntArray(Base.kNumRepDistances)

    private fun getOptimum(position: Int): Pair<Int, Int> {
        if (_optimumEndIndex != _optimumCurrentIndex) {
            val lenRes = _optimum[_optimumCurrentIndex].posPrev - _optimumCurrentIndex
            val backRes = _optimum[_optimumCurrentIndex].backPrev
            _optimumCurrentIndex = _optimum[_optimumCurrentIndex].posPrev
            return Pair(lenRes, backRes)
        }
        _optimumCurrentIndex = 0
        _optimumEndIndex = 0

        var lenMain: Int
        var numDistancePairs: Int
        if (!_longestMatchWasFound) {
            val matchDistances = readMatchDistances()
            lenMain = matchDistances.first
            numDistancePairs = matchDistances.second
        } else {
            lenMain = _longestMatchLength
            numDistancePairs = _numDistancePairs
            _longestMatchWasFound = false
        }

        var numAvailableBytes = _matchFinder!!.getNumAvailableBytes() + 1
        if (numAvailableBytes < 2) {
            return Pair(1, -1)
        }
        if (numAvailableBytes > Base.kMatchMaxLen) {
            numAvailableBytes = Base.kMatchMaxLen
        }

        var repMaxIndex = 0
        for (i in 0 until Base.kNumRepDistances) {
            reps[i] = _repDistances[i]
            repLens[i] = _matchFinder!!.getMatchLen(0 - 1, reps[i], Base.kMatchMaxLen)
            if (repLens[i] > repLens[repMaxIndex]) {
                repMaxIndex = i
            }
        }
        if (repLens[repMaxIndex] >= _numFastBytes) {
            val lenRes = repLens[repMaxIndex]
            movePos(lenRes - 1)
            return Pair(lenRes, repMaxIndex)
        }

        if (lenMain >= _numFastBytes) {
            val backRes = _matchDistances[numDistancePairs - 1] + Base.kNumRepDistances
            movePos(lenMain - 1)
            return Pair(lenMain, backRes)
        }

        var currentByte = _matchFinder!!.getIndexByte(0 - 1)
        var matchByte = _matchFinder!!.getIndexByte(0 - _repDistances[0] - 1 - 1)

        if (lenMain < 2 && currentByte != matchByte && repLens[repMaxIndex] < 2) {
            return Pair(1, -1)
        }

        _optimum[0].state = _state

        var localPos = position
        val posState = localPos and _posStateMask

        _optimum[1].price = _isMatch[(_state.index shl Base.kNumPosStatesBitsMax) + posState].getPrice0() +
                _literalEncoder.getSubCoder(localPos, _previousByte).getPrice(!_state.isCharState(), matchByte, currentByte)
        _optimum[1].makeAsChar()

        var matchPrice = _isMatch[(_state.index shl Base.kNumPosStatesBitsMax) + posState].getPrice1()
        var repMatchPrice = matchPrice + _isRep[_state.index].getPrice1()

        if (matchByte == currentByte) {
            val shortRepPrice = repMatchPrice + getRepLen1Price(_state, posState)
            if (shortRepPrice < _optimum[1].price) {
                _optimum[1].price = shortRepPrice
                _optimum[1].makeAsShortRep()
            }
        }

        var lenEnd = if (lenMain >= repLens[repMaxIndex]) lenMain else repLens[repMaxIndex]

        if (lenEnd < 2) {
            return Pair(1, _optimum[1].backPrev)
        }

        _optimum[1].posPrev = 0

        _optimum[0].backs0 = reps[0]
        _optimum[0].backs1 = reps[1]
        _optimum[0].backs2 = reps[2]
        _optimum[0].backs3 = reps[3]

        var len = lenEnd
        do {
            _optimum[len--].price = kIfinityPrice
        } while (len >= 2)

        for (i in 0 until Base.kNumRepDistances) {
            var repLen = repLens[i]
            if (repLen < 2) {
                continue
            }
            val price = repMatchPrice + getPureRepPrice(i, _state, posState)
            do {
                val curAndLenPrice = price + _repMatchLenEncoder.getPrice(repLen - 2, posState)
                val optimum = _optimum[repLen]
                if (curAndLenPrice < optimum.price) {
                    optimum.price = curAndLenPrice
                    optimum.posPrev = 0
                    optimum.backPrev = i
                    optimum.prev1IsChar = false
                }
            } while (--repLen >= 2)
        }

        var normalMatchPrice = matchPrice + _isRep[_state.index].getPrice0()

        len = if (repLens[0] >= 2) repLens[0] + 1 else 2
        if (len <= lenMain) {
            var offs = 0
            while (len > _matchDistances[offs]) {
                offs += 2
            }
            while (true) {
                val distance = _matchDistances[offs + 1]
                val curAndLenPrice = normalMatchPrice + getPosLenPrice(distance, len, posState)
                val optimum = _optimum[len]
                if (curAndLenPrice < optimum.price) {
                    optimum.price = curAndLenPrice
                    optimum.posPrev = 0
                    optimum.backPrev = distance + Base.kNumRepDistances
                    optimum.prev1IsChar = false
                }
                if (len == _matchDistances[offs]) {
                    offs += 2
                    if (offs == numDistancePairs) {
                        break
                    }
                }
                len++
            }
        }

        var cur = 0

        while (true) {
            cur++
            if (cur == lenEnd) {
                return backward(cur)
            }
            var newLen: Int
            var numDistancePairs: Int
            val matchDistances = readMatchDistances()
            newLen = matchDistances.first
            numDistancePairs = matchDistances.second
            if (newLen >= _numFastBytes) {
                _numDistancePairs = numDistancePairs
                _longestMatchLength = newLen
                _longestMatchWasFound = true
                return backward(cur)
            }
            localPos++
            var posPrev = _optimum[cur].posPrev
            var state: State
            if (_optimum[cur].prev1IsChar) {
                posPrev--
                if (_optimum[cur].prev2) {
                    state = _optimum[_optimum[cur].posPrev2].state
                    if (_optimum[cur].backPrev2 < Base.kNumRepDistances) {
                        state.updateRep()
                    } else {
                        state.updateMatch()
                    }
                } else {
                    state = _optimum[posPrev].state
                }
                state.updateChar()
            } else {
                state = _optimum[posPrev].state
            }
            if (posPrev == cur - 1) {
                if (_optimum[cur].isShortRep()) {
                    state.updateShortRep()
                } else {
                    state.updateChar()
                }
            } else {
                var pos: Int
                if (_optimum[cur].prev1IsChar && _optimum[cur].prev2) {
                    posPrev = _optimum[cur].posPrev2
                    pos = _optimum[cur].backPrev2
                    state.updateRep()
                } else {
                    pos = _optimum[cur].backPrev
                    if (pos < Base.kNumRepDistances) {
                        state.updateRep()
                    } else {
                        state.updateMatch()
                    }
                }
                val opt = _optimum[posPrev]
                if (pos < Base.kNumRepDistances) {
                    when (pos) {
                        0 -> {
                            reps[0] = opt.backs0
                            reps[1] = opt.backs1
                            reps[2] = opt.backs2
                            reps[3] = opt.backs3
                        }
                        1 -> {
                            reps[0] = opt.backs1
                            reps[1] = opt.backs0
                            reps[2] = opt.backs2
                            reps[3] = opt.backs3
                        }
                        2 -> {
                            reps[0] = opt.backs2
                            reps[1] = opt.backs0
                            reps[2] = opt.backs1
                            reps[3] = opt.backs3
                        }
                        else -> {
                            reps[0] = opt.backs3
                            reps[1] = opt.backs0
                            reps[2] = opt.backs1
                            reps[3] = opt.backs2
                        }
                    }
                } else {
                    reps[0] = pos - Base.kNumRepDistances
                    reps[1] = opt.backs0
                    reps[2] = opt.backs1
                    reps[3] = opt.backs2
                }
            }
            _optimum[cur].state = state
            _optimum[cur].backs0 = reps[0]
            _optimum[cur].backs1 = reps[1]
            _optimum[cur].backs2 = reps[2]
            _optimum[cur].backs3 = reps[3]
            val curPrice = _optimum[cur].price

            currentByte = _matchFinder!!.getIndexByte(0 - 1)
            matchByte = _matchFinder!!.getIndexByte(0 - reps[0] - 1 - 1)

            val posState = localPos and _posStateMask

            val curAnd1Price = curPrice +
                    _isMatch[(state.index shl Base.kNumPosStatesBitsMax) + posState].getPrice0() +
                    _literalEncoder.getSubCoder(localPos, _matchFinder!!.getIndexByte(0 - 2))
                        .getPrice(!state.isCharState(), matchByte, currentByte)

            val nextOptimum = _optimum[cur + 1]

            var nextIsChar = false
            if (curAnd1Price < nextOptimum.price) {
                nextOptimum.price = curAnd1Price
                nextOptimum.posPrev = cur
                nextOptimum.makeAsChar()
                nextIsChar = true
            }

            matchPrice = curPrice + _isMatch[(state.index shl Base.kNumPosStatesBitsMax) + posState].getPrice1()
            repMatchPrice = matchPrice + _isRep[state.index].getPrice1()

            if (matchByte == currentByte &&
                !(nextOptimum.posPrev < cur && nextOptimum.backPrev == 0)
            ) {
                val shortRepPrice = repMatchPrice + getRepLen1Price(state, posState)
                if (shortRepPrice <= nextOptimum.price) {
                    nextOptimum.price = shortRepPrice
                    nextOptimum.posPrev = cur
                    nextOptimum.makeAsShortRep()
                    nextIsChar = true
                }
            }

            var numAvailableBytesFull = _matchFinder!!.getNumAvailableBytes() + 1
            numAvailableBytesFull = minOf(kNumOpts - 1 - cur, numAvailableBytesFull)
            numAvailableBytes = numAvailableBytesFull

            if (numAvailableBytes < 2) {
                continue
            }
            if (numAvailableBytes > _numFastBytes) {
                numAvailableBytes = _numFastBytes
            }
            if (!nextIsChar && matchByte != currentByte) {
                // try Literal + rep0
                val t = minOf(numAvailableBytesFull - 1, _numFastBytes)
                val lenTest2 = _matchFinder!!.getMatchLen(0, reps[0], t)
                if (lenTest2 >= 2) {
                    val state2 = state
                    state2.updateChar()
                    val posStateNext = (localPos + 1) and _posStateMask
                    val nextRepMatchPrice = curAnd1Price +
                            _isMatch[(state2.index shl Base.kNumPosStatesBitsMax) + posStateNext].getPrice1() +
                            _isRep[state2.index].getPrice1()
                    run {
                        val offset = cur + 1 + lenTest2
                        while (lenEnd < offset) {
                            _optimum[++lenEnd].price = kIfinityPrice
                        }
                        val curAndLenPrice = nextRepMatchPrice + getRepPrice(
                            0, lenTest2, state2, posStateNext
                        )
                        val optimum = _optimum[offset]
                        if (curAndLenPrice < optimum.price) {
                            optimum.price = curAndLenPrice
                            optimum.posPrev = cur + 1
                            optimum.backPrev = 0
                            optimum.prev1IsChar = true
                            optimum.prev2 = false
                        }
                    }
                }
            }

            var startLen = 2 // speed optimization

            for (repIndex in 0 until Base.kNumRepDistances) {
                var lenTest = _matchFinder!!.getMatchLen(0 - 1, reps[repIndex], numAvailableBytes)
                if (lenTest < 2) {
                    continue
                }
                val lenTestTemp = lenTest
                do {
                    while (lenEnd < cur + lenTest) {
                        _optimum[++lenEnd].price = kIfinityPrice
                    }
                    val curAndLenPrice = repMatchPrice + getRepPrice(repIndex, lenTest, state, posState)
                    val optimum = _optimum[cur + lenTest]
                    if (curAndLenPrice < optimum.price) {
                        optimum.price = curAndLenPrice
                        optimum.posPrev = cur
                        optimum.backPrev = repIndex
                        optimum.prev1IsChar = false
                    }
                } while (--lenTest >= 2)
                lenTest = lenTestTemp

                if (repIndex == 0) {
                    startLen = lenTest + 1
                }

                // if (_maxMode)
                if (lenTest < numAvailableBytesFull) {
                    val t = minOf(numAvailableBytesFull - 1 - lenTest, _numFastBytes)
                    val lenTest2 = _matchFinder!!.getMatchLen(lenTest, reps[repIndex], t)
                    if (lenTest2 >= 2) {
                        val state2 = state
                        state2.updateRep()
                        val posStateNext = (localPos + lenTest) and _posStateMask
                        val curAndLenCharPrice =
                            repMatchPrice + getRepPrice(repIndex, lenTest, state, posState) +
                                    _isMatch[(state2.index shl Base.kNumPosStatesBitsMax) + posStateNext].getPrice0() +
                                    _literalEncoder.getSubCoder(
                                        localPos + lenTest,
                                        _matchFinder!!.getIndexByte(lenTest - 1 - 1)
                                    ).getPrice(
                                        true,
                                        _matchFinder!!.getIndexByte(lenTest - 1 - (reps[repIndex] + 1)),
                                        _matchFinder!!.getIndexByte(lenTest - 1)
                                    )
                        state2.updateChar()
                        val posStateNext2 = (localPos + lenTest + 1) and _posStateMask
                        val nextMatchPrice = curAndLenCharPrice + _isMatch[(state2.index shl Base.kNumPosStatesBitsMax) + posStateNext2].getPrice1()
                        val nextRepMatchPrice = nextMatchPrice + _isRep[state2.index].getPrice1()

                        // for(; lenTest2 >= 2; lenTest2--)
                        run {
                            val offset = lenTest + 1 + lenTest2
                            while (lenEnd < cur + offset) {
                                _optimum[++lenEnd].price = kIfinityPrice
                            }
                            val curAndLenPrice = nextRepMatchPrice + getRepPrice(0, lenTest2, state2, posStateNext2)
                            val optimum = _optimum[cur + offset]
                            if (curAndLenPrice < optimum.price) {
                                optimum.price = curAndLenPrice
                                optimum.posPrev = cur + lenTest + 1
                                optimum.backPrev = 0
                                optimum.prev1IsChar = true
                                optimum.prev2 = true
                                optimum.posPrev2 = cur
                                optimum.backPrev2 = repIndex
                            }
                        }
                    }
                }
            }

            if (newLen > numAvailableBytes) {
                newLen = numAvailableBytes
                for (numDistancePairs2 in 0 until numDistancePairs step 2) {
                if (newLen > _matchDistances[numDistancePairs2]) {
                    numDistancePairs = numDistancePairs2
                    break
                }
            }
            _matchDistances[numDistancePairs] = newLen
            numDistancePairs += 2
        }
        if (newLen >= startLen) {
            normalMatchPrice = matchPrice + _isRep[state.index].getPrice0()
            while (lenEnd < cur + newLen) {
                _optimum[++lenEnd].price = kIfinityPrice
            }

            var offs = 0
            while (startLen > _matchDistances[offs]) {
                offs += 2
            }

            for (lenTest in startLen until newLen + 1) {
                val curBack = _matchDistances[offs + 1]
                val curAndLenPrice = normalMatchPrice + getPosLenPrice(curBack, lenTest, posState)
                val optimum = _optimum[cur + lenTest]
                if (curAndLenPrice < optimum.price) {
                    optimum.price = curAndLenPrice
                    optimum.posPrev = cur
                    optimum.backPrev = curBack + Base.kNumRepDistances
                    optimum.prev1IsChar = false
                }

                if (lenTest == _matchDistances[offs]) {
                    offs += 2
                    if (offs == numDistancePairs) {
                        break
                    }
                }
            }
        }
    }
    }

    private fun changePair(smallDist: Int, bigDist: Int): Boolean {
        val kDif = 7
        return smallDist < (1 shl (32 - kDif)) && bigDist >= (smallDist shl kDif)
    }

    private fun writeEndMarker(posState: Int) {
        if (!_writeEndMark) {
            return
        }

        _isMatch[(_state.index shl Base.kNumPosStatesBitsMax) + posState].encode(_rangeEncoder, 1)
        _isRep[_state.index].encode(_rangeEncoder, 0)
        _state.updateMatch()
        val len = Base.kMatchMinLen
        _lenEncoder.encode(_rangeEncoder, len - Base.kMatchMinLen, posState)
        val posSlot = (1 shl Base.kNumPosSlotBits) - 1
        val lenToPosState = Base.getLenToPosState(len)
        _posSlotEncoder[lenToPosState].encode(_rangeEncoder, posSlot)
        val footerBits = 30
        val posReduced = (1 shl footerBits) - 1
        _rangeEncoder.encodeDirectBits(posReduced shr Base.kNumAlignBits, footerBits - Base.kNumAlignBits)
        _posAlignEncoder.reverseEncode(_rangeEncoder, posReduced and Base.kAlignMask)
    }

    private fun flush(nowPos: Int) {
        releaseMFStream()
        writeEndMarker(nowPos and _posStateMask)
        _rangeEncoder.flushData()
        _rangeEncoder.flushStream()
    }

    fun codeOneBlock(inSize: Long, outSize: Long, finished: Boolean): Triple<Long, Long, Boolean> {
        var inSize = inSize
        var outSize = outSize
        var finished = finished

        if (_inStream != null) {
            _matchFinder!!.setStream(_inStream!!)
            _matchFinder!!.init()
            _needReleaseMFStream = true
            _inStream = null
            if (_trainSize > 0) {
                _matchFinder!!.skip(_trainSize)
            }
        }

        if (_finished) {
            return Triple(inSize, outSize, finished)
        }
        _finished = true

        val progressPosValuePrev = nowPos64
        if (nowPos64 == 0L) {
            if (_matchFinder!!.getNumAvailableBytes() == 0) {
                flush(nowPos64.toInt())
                return Triple(inSize, outSize, finished)
            }
            val (len, numDistancePairs) = readMatchDistances()
            val posState = nowPos64.toInt() and _posStateMask
            _isMatch[(_state.index shl Base.kNumPosStatesBitsMax) + posState].encode(_rangeEncoder, 0)
            _state.updateChar()
            val curByte = _matchFinder!!.getIndexByte(0 - _additionalOffset)
            _literalEncoder.getSubCoder(nowPos64.toInt(), _previousByte).encode(_rangeEncoder, curByte)
            _previousByte = curByte
            _additionalOffset--
            nowPos64++
        }
        if (_matchFinder!!.getNumAvailableBytes() == 0) {
            flush(nowPos64.toInt())
            return Triple(inSize, outSize, finished)
        }
        while (true) {
            var (len, pos) = getOptimum(nowPos64.toInt())

            val posState = nowPos64.toInt() and _posStateMask
            val complexState = (_state.index shl Base.kNumPosStatesBitsMax) + posState
            if (len == 1 && pos == -1) {
                _isMatch[complexState].encode(_rangeEncoder, 0)
                val curByte = _matchFinder!!.getIndexByte(0 - _additionalOffset)
                val subCoder = _literalEncoder.getSubCoder(nowPos64.toInt(), _previousByte)
                if (!_state.isCharState()) {
                    val matchByte = _matchFinder!!.getIndexByte(0 - _repDistances[0] - 1 - _additionalOffset)
                    subCoder.encodeMatched(_rangeEncoder, matchByte, curByte)
                } else {
                    subCoder.encode(_rangeEncoder, curByte)
                }
                _previousByte = curByte
                _state.updateChar()
            } else {
                _isMatch[complexState].encode(_rangeEncoder, 1)
                if (pos < Base.kNumRepDistances) {
                    _isRep[_state.index].encode(_rangeEncoder, 1)
                    if (pos == 0) {
                        _isRepG0[_state.index].encode(_rangeEncoder, 0)
                        if (len == 1) {
                            _isRep0Long[complexState].encode(_rangeEncoder, 0)
                        } else {
                            _isRep0Long[complexState].encode(_rangeEncoder, 1)
                        }
                    } else {
                        _isRepG0[_state.index].encode(_rangeEncoder, 1)
                        if (pos == 1) {
                            _isRepG1[_state.index].encode(_rangeEncoder, 0)
                        } else {
                            _isRepG1[_state.index].encode(_rangeEncoder, 1)
                            _isRepG2[_state.index].encode(_rangeEncoder, pos - 2)
                        }
                    }
                    if (len == 1) {
                        _state.updateShortRep()
                    } else {
                        _repMatchLenEncoder.encode(_rangeEncoder, len - Base.kMatchMinLen, posState)
                        _state.updateRep()
                    }
                    val distance = _repDistances[pos]
                    if (pos != 0) {
                        for (i in pos downTo 1) {
                            _repDistances[i] = _repDistances[i - 1]
                        }
                        _repDistances[0] = distance
                    }
                } else {
                    _isRep[_state.index].encode(_rangeEncoder, 0)
                    _state.updateMatch()
                    _lenEncoder.encode(_rangeEncoder, len - Base.kMatchMinLen, posState)
                    pos -= Base.kNumRepDistances
                    val posSlot = getPosSlot(pos)
                    val lenToPosState = Base.getLenToPosState(len)
                    _posSlotEncoder[lenToPosState].encode(_rangeEncoder, posSlot)

                    if (posSlot >= Base.kStartPosModelIndex) {
                        val footerBits = (posSlot shr 1) - 1
                        val baseVal = (2 or (posSlot and 1)) shl footerBits
                        val posReduced = pos - baseVal

                        if (posSlot < Base.kEndPosModelIndex) {
                            BitTreeEncoder.reverseEncode(
                                _posEncoders,
                                baseVal - posSlot - 1, _rangeEncoder, footerBits, posReduced
                            )
                        } else {
                            _rangeEncoder.encodeDirectBits(posReduced shr Base.kNumAlignBits, footerBits - Base.kNumAlignBits)
                            _posAlignEncoder.reverseEncode(_rangeEncoder, posReduced and Base.kAlignMask)
                            _alignPriceCount++
                        }
                    }
                    val distance = pos
                    for (i in Base.kNumRepDistances - 1 downTo 1) {
                        _repDistances[i] = _repDistances[i - 1]
                    }
                    _repDistances[0] = distance
                    _matchPriceCount++
                }
                _previousByte = _matchFinder!!.getIndexByte(len - 1 - _additionalOffset)
            }
            _additionalOffset -= len
            nowPos64 += len
            if (_additionalOffset == 0) {
                // if (!_fastMode)
                if (_matchPriceCount >= (1 shl 7)) {
                    fillDistancesPrices()
                }
                if (_alignPriceCount >= Base.kAlignTableSize) {
                    fillAlignPrices()
                }
                inSize = nowPos64
                outSize = _rangeEncoder.getProcessedSizeAdd()
                if (_matchFinder!!.getNumAvailableBytes() == 0) {
                    flush(nowPos64.toInt())
                    return Triple(inSize, outSize, finished)
                }

                if (nowPos64 - progressPosValuePrev >= (1 shl 12)) {
                    _finished = false
                    finished = false
                    return Triple(inSize, outSize, finished)
                }
            }
        }
    }

    private fun releaseMFStream() {
        if (_matchFinder != null && _needReleaseMFStream) {
            _matchFinder!!.releaseStream()
            _needReleaseMFStream = false
        }
    }

    private fun setOutStream(outStream: OutputStream) {
        _rangeEncoder.setStream(outStream)
    }

    private fun releaseOutStream() {
        _rangeEncoder.releaseStream()
    }

    private fun releaseStreams() {
        releaseMFStream()
        releaseOutStream()
    }

    fun setStreams(inStream: InputStream, outStream: OutputStream, inSize: Long, outSize: Long) {
        _inStream = inStream
        _finished = false
        create()
        setOutStream(outStream)
        init()

        // if (!_fastMode)
        fillDistancesPrices()
        fillAlignPrices()

        _lenEncoder.setTableSize(_numFastBytes + 1 - Base.kMatchMinLen)
        _lenEncoder.updateTables(1 shl _posStateBits)
        _repMatchLenEncoder.setTableSize(_numFastBytes + 1 - Base.kMatchMinLen)
        _repMatchLenEncoder.updateTables(1 shl _posStateBits)

        nowPos64 = 0
    }

    override fun code(inStream: InputStream, outStream: OutputStream, inSize: Long, outSize: Long, progress: ICodeProgress?) {
        _needReleaseMFStream = false
        try {
            setStreams(inStream, outStream, inSize, outSize)
            while (true) {
                val (processedInSize, processedOutSize, finished) = codeOneBlock(0, 0, false)
                if (finished) {
                    return
                }
                progress?.setProgress(processedInSize, processedOutSize)
            }
        } finally {
            releaseStreams()
        }
    }

    override fun writeCoderProperties(outStream: OutputStream) {
        properties[0] = ((_posStateBits * 5 + _numLiteralPosStateBits) * 9 + _numLiteralContextBits).toByte()
        for (i in 0 until 4) {
            properties[1 + i] = (_dictionarySize shr (8 * i)).toByte()
        }
        outStream.write(properties, 0, kPropSize)
    }

    private val tempPrices = IntArray(Base.kNumFullDistances)
    private var _matchPriceCount = 0

    private fun fillDistancesPrices() {
        for (i in Base.kStartPosModelIndex until Base.kNumFullDistances) {
            val posSlot = getPosSlot(i)
            val footerBits = (posSlot shr 1) - 1
            val baseVal = (2 or (posSlot and 1)) shl footerBits
            tempPrices[i] = BitTreeEncoder.reverseGetPrice(
                _posEncoders,
                baseVal - posSlot - 1, footerBits, i - baseVal
            )
        }

        for (lenToPosState in 0 until Base.kNumLenToPosStates) {
            val encoder = _posSlotEncoder[lenToPosState]

            val st = lenToPosState shl Base.kNumPosSlotBits
            for (posSlot in 0 until _distTableSize) {
                _posSlotPrices[st + posSlot] = encoder.getPrice(posSlot)
            }
            for (posSlot in Base.kEndPosModelIndex until _distTableSize) {
                _posSlotPrices[st + posSlot] += ((posSlot shr 1) - 1 - Base.kNumAlignBits) shl BitEncoder.kNumBitPriceShiftBits
            }

            val st2 = lenToPosState * Base.kNumFullDistances
            var i = 0
            while (i < Base.kStartPosModelIndex) {
                _distancesPrices[st2 + i] = _posSlotPrices[st + i]
                i++
            }
            while (i < Base.kNumFullDistances) {
                _distancesPrices[st2 + i] = _posSlotPrices[st + getPosSlot(i)] + tempPrices[i]
                i++
            }
        }
        _matchPriceCount = 0
    }

    private fun fillAlignPrices() {
        for (i in 0 until Base.kAlignTableSize) {
            _alignPrices[i] = _posAlignEncoder.reverseGetPrice(i)
        }
        _alignPriceCount = 0
    }

    private val kMatchFinderIDs = arrayOf(
        "BT2",
        "BT4"
    )

    private fun findMatchFinder(s: String): Int {
        for (m in kMatchFinderIDs.indices) {
            if (s == kMatchFinderIDs[m]) {
                return m
            }
        }
        return -1
    }

    override fun setCoderProperties(propIDs: Array<CoderPropID>, properties: Array<Any>) {
        for (i in properties.indices) {
            val prop = properties[i]
            when (propIDs[i]) {
                CoderPropID.NumFastBytes -> {
                    if (prop !is Int) {
                        throw InvalidParamException()
                    }
                    val numFastBytes = prop
                    if (numFastBytes < 5 || numFastBytes > Base.kMatchMaxLen) {
                        throw InvalidParamException()
                    }
                    _numFastBytes = numFastBytes
                }
                CoderPropID.Algorithm -> {
                    // Ignored in this implementation
                }
                CoderPropID.MatchFinder -> {
                    if (prop !is String) {
                        throw InvalidParamException()
                    }
                    val matchFinderIndexPrev = _matchFinderType
                    val m = findMatchFinder(prop.toUpperCase())
                    if (m < 0) {
                        throw InvalidParamException()
                    }
                    _matchFinderType = EMatchFinderType.values()[m]
                    if (_matchFinder != null && matchFinderIndexPrev != _matchFinderType) {
                        _dictionarySizePrev = -1
                        _matchFinder = null
                    }
                }
                CoderPropID.DictionarySize -> {
                    val kDicLogSizeMaxCompress = 30
                    if (prop !is Int) {
                        throw InvalidParamException()
                    }
                    val dictionarySize = prop.toInt()
                    if ((dictionarySize < (1 shl Base.kDicLogSizeMin)) ||
                        (dictionarySize > (1 shl kDicLogSizeMaxCompress))
                    ) {
                        throw InvalidParamException()
                    }
                    _dictionarySize = dictionarySize
                    var dicLogSize = 0
                    while (dicLogSize < kDicLogSizeMaxCompress) {
                        if (dictionarySize <= (1 shl dicLogSize)) {
                            break
                        }
                        dicLogSize++
                    }
                    _distTableSize = dicLogSize * 2
                }
                CoderPropID.PosStateBits -> {
                    if (prop !is Int) {
                        throw InvalidParamException()
                    }
                    val v = prop
                    if (v < 0 || v > Base.kNumPosStatesBitsEncodingMax) {
                        throw InvalidParamException()
                    }
                    _posStateBits = v
                    _posStateMask = (1 shl _posStateBits) - 1
                }
                CoderPropID.LitPosBits -> {
                    if (prop !is Int) {
                        throw InvalidParamException()
                    }
                    val v = prop
                    if (v < 0 || v > Base.kNumLitPosStatesBitsEncodingMax) {
                        throw InvalidParamException()
                    }
                    _numLiteralPosStateBits = v
                }
                CoderPropID.LitContextBits -> {
                    if (prop !is Int) {
                        throw InvalidParamException()
                    }
                    val v = prop
                    if (v < 0 || v > Base.kNumLitContextBitsMax) {
                        throw InvalidParamException()
                    }
                    _numLiteralContextBits = v
                }
                CoderPropID.EndMarker -> {
                    if (prop !is Boolean) {
                        throw InvalidParamException()
                    }
                    setWriteEndMarkerMode(prop)
                }
                else -> throw InvalidParamException()
            }
        }
    }

    private var _trainSize = 0
    fun setTrainSize(trainSize: Int) {
        _trainSize = trainSize
    }
}
