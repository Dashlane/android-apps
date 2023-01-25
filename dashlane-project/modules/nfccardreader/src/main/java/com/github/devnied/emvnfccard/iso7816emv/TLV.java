

package com.github.devnied.emvnfccard.iso7816emv;



public class TLV {

    

    private ITag tag;
    

    private byte[] rawEncodedLengthBytes;
    

    private byte[] valueBytes;
    

    private int length;

    

    public TLV(final ITag tag, final int length, final byte[] rawEncodedLengthBytes, final byte[] valueBytes) {
        if (valueBytes == null || length != valueBytes.length) {
            
            throw new IllegalArgumentException("length != bytes.length");
        }
        this.tag = tag;
        this.rawEncodedLengthBytes = rawEncodedLengthBytes;
        this.valueBytes = valueBytes;
        this.length = length;
    }

    

    public ITag getTag() {
        return tag;
    }

    

    public void setTag(final ITag tag) {
        this.tag = tag;
    }

    

    public byte[] getRawEncodedLengthBytes() {
        return rawEncodedLengthBytes;
    }

    

    public void setRawEncodedLengthBytes(final byte[] rawEncodedLengthBytes) {
        this.rawEncodedLengthBytes = rawEncodedLengthBytes;
    }

    

    public byte[] getValueBytes() {
        return valueBytes;
    }

    

    public void setValueBytes(final byte[] valueBytes) {
        this.valueBytes = valueBytes;
    }

    

    public int getLength() {
        return length;
    }

    

    public void setLength(final int length) {
        this.length = length;
    }

    

    public byte[] getTagBytes() {
        return tag.getTagBytes();
    }

}
