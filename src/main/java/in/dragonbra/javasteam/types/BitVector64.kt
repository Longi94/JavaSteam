package `in`.dragonbra.javasteam.types

class BitVector64(var data: Long) {

    fun getMask(bitOffset: Int, valueMask: Long): Long = (data shr bitOffset) and valueMask

    fun setMask(bitOffset: Int, valueMask: Long, value: Long) {
        data = (data and (valueMask shl bitOffset).inv()) or ((value and valueMask) shl bitOffset)
    }
}
