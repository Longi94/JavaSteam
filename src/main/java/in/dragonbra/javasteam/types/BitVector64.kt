package in.dragonbra.javasteam.types;

public class BitVector64 {

    private Long data;

    public BitVector64() {
    }

    public BitVector64(long value) {
        data = value;
    }

    public Long getData() {
        return data;
    }

    public void setData(Long data) {
        this.data = data;
    }

    public long getMask(short bitOffset, long valueMask) {
        return data >> bitOffset & valueMask;
    }

    public void setMask(short bitOffset, long valueMask, long value) {
        data = (data & ~(valueMask << bitOffset)) | ((value & valueMask) << bitOffset);
    }
}
