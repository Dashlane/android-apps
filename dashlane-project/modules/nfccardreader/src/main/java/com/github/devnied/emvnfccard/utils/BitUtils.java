package com.github.devnied.emvnfccard.utils;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public final class BitUtils {
    public static final int BYTE_SIZE = Byte.SIZE;
    public static final float BYTE_SIZE_F = Byte.SIZE;
    private static final int DEFAULT_VALUE = 0xFF;
    private static final Charset DEFAULT_CHARSET = Charset.forName("ASCII");

    public static final String DATE_FORMAT = "yyyyMMdd";

    private final byte[] byteTab;

    private int currentBitIndex;

    private final int size;

    public BitUtils(final byte pByte[]) {
        byteTab = new byte[pByte.length];
        System.arraycopy(pByte, 0, byteTab, 0, pByte.length);
        size = pByte.length * BYTE_SIZE;
    }

    public BitUtils(final int pSize) {
        byteTab = new byte[(int) Math.ceil(pSize / BYTE_SIZE_F)];
        size = pSize;
    }

    public void addCurrentBitIndex(final int pIndex) {
        currentBitIndex += pIndex;
        if (currentBitIndex < 0) {
            currentBitIndex = 0;
        }
    }

    public int getCurrentBitIndex() {
        return currentBitIndex;
    }

    public byte[] getData() {
        byte[] ret = new byte[byteTab.length];
        System.arraycopy(byteTab, 0, ret, 0, byteTab.length);
        return ret;
    }

    public byte getMask(final int pIndex, final int pLength) {
        byte ret = (byte) DEFAULT_VALUE;
        
        ret = (byte) (ret << pIndex);
        ret = (byte) ((ret & DEFAULT_VALUE) >> pIndex);
        
        int dec = BYTE_SIZE - (pLength + pIndex);
        if (dec > 0) {
            ret = (byte) (ret >> dec);
            ret = (byte) (ret << dec);
        }
        return ret;
    }

    public boolean getNextBoolean() {
        boolean ret = false;
        if (getNextInteger(1) == 1) {
            ret = true;
        }
        return ret;
    }

    public byte[] getNextByte(final int pSize) {
        return getNextByte(pSize, true);
    }

    
    @SuppressWarnings("squid:S3776")
    public byte[] getNextByte(final int pSize, final boolean pShift) {
        byte[] tab = new byte[(int) Math.ceil(pSize / BYTE_SIZE_F)];

        if (currentBitIndex % BYTE_SIZE != 0) {
            int index = 0;
            int max = currentBitIndex + pSize;
            while (currentBitIndex < max) {
                int mod = currentBitIndex % BYTE_SIZE;
                int modTab = index % BYTE_SIZE;
                int length = Math.min(max - currentBitIndex, Math.min(BYTE_SIZE - mod, BYTE_SIZE - modTab));
                byte val = (byte) (byteTab[currentBitIndex / BYTE_SIZE] & getMask(mod, length));
                if (pShift || pSize % BYTE_SIZE == 0) {
                    if (mod != 0) {
                        val = (byte) (val << Math.min(mod, BYTE_SIZE - length));
                    } else {
                        val = (byte) ((val & DEFAULT_VALUE) >> modTab);
                    }
                }
                tab[index / BYTE_SIZE] |= val;
                currentBitIndex += length;
                index += length;
            }
            if (!pShift && pSize % BYTE_SIZE != 0) {
                tab[tab.length - 1] = (byte) (tab[tab.length - 1] & getMask((max - pSize - 1) % BYTE_SIZE, BYTE_SIZE));
            }
        } else {
            System.arraycopy(byteTab, currentBitIndex / BYTE_SIZE, tab, 0, tab.length);
            int val = pSize % BYTE_SIZE;
            if (val == 0) {
                val = BYTE_SIZE;
            }
            tab[tab.length - 1] = (byte) (tab[tab.length - 1] & getMask(currentBitIndex % BYTE_SIZE, val));
            currentBitIndex += pSize;
        }

        return tab;
    }

    public Date getNextDate(final int pSize, final String pPattern) {
        return getNextDate(pSize, pPattern, false);
    }

    public Date getNextDate(final int pSize, final String pPattern, final boolean pUseBcd) {
        Date date = null;
        
        SimpleDateFormat sdf = new SimpleDateFormat(pPattern, Locale.US);
        
        String dateTxt = null;
        if (pUseBcd) {
            dateTxt = getNextHexaString(pSize);
        } else {
            dateTxt = getNextString(pSize);
        }

        try {
            date = sdf.parse(dateTxt);
        } catch (ParseException e) {
            
        }
        return date;
    }

    public String getNextHexaString(final int pSize) {
        return BytesUtils.bytesToStringNoSpace(getNextByte(pSize, true));
    }

    public long getNextLongSigned(final int pLength) {
        if (pLength > Long.SIZE) {
            throw new IllegalArgumentException("Long overflow with length > 64");
        }
        long decimal = getNextLong(pLength);
        long signMask = 1 << pLength - 1;

        if ( (decimal & signMask) != 0) {
            return - (signMask - (signMask ^ decimal));
        }
        return decimal;
    }

    public int getNextIntegerSigned(final int pLength) {
        if (pLength > Integer.SIZE) {
            throw new IllegalArgumentException("Integer overflow with length > 32");
        }
        return (int) getNextLongSigned(pLength);
    }

    public long getNextLong(final int pLength) {
        
        ByteBuffer buffer = ByteBuffer.allocate(BYTE_SIZE * 2);
        
        long finalValue = 0;
        
        long currentValue = 0;
        
        int readSize = pLength;
        
        int max = currentBitIndex + pLength;
        while (currentBitIndex < max) {
            int mod = currentBitIndex % BYTE_SIZE;
            
            currentValue = byteTab[currentBitIndex / BYTE_SIZE] & getMask(mod, readSize) & DEFAULT_VALUE;
            
            int dec = Math.max(BYTE_SIZE - (mod + readSize), 0);
            currentValue = (currentValue & DEFAULT_VALUE) >>> dec & DEFAULT_VALUE;
            
            finalValue = finalValue << Math.min(readSize, BYTE_SIZE) | currentValue;
            
            int val = BYTE_SIZE - mod;
            
            readSize = readSize - val;
            currentBitIndex = Math.min(currentBitIndex + val, max);
        }
        buffer.putLong(finalValue);
        
        buffer.rewind();
        
        return buffer.getLong();
    }

    public int getNextInteger(final int pLength) {
        return (int) (getNextLong(pLength));
    }

    public String getNextString(final int pSize) {
        return getNextString(pSize, DEFAULT_CHARSET);
    }

    public String getNextString(final int pSize, final Charset pCharset) {
        return new String(getNextByte(pSize, true), pCharset);
    }

    public int getSize() {
        return size;
    }

    public void reset() {
        setCurrentBitIndex(0);
    }

    public void clear() {
        Arrays.fill(byteTab, (byte) 0);
        reset();
    }

    public void resetNextBits(final int pLength) {
        int max = currentBitIndex + pLength;
        while (currentBitIndex < max) {
            int mod = currentBitIndex % BYTE_SIZE;
            int length = Math.min(max - currentBitIndex, BYTE_SIZE - mod);
            byteTab[currentBitIndex / BYTE_SIZE] &= ~getMask(mod, length);
            currentBitIndex += length;
        }
    }

    public void setCurrentBitIndex(final int pCurrentBitIndex) {
        currentBitIndex = pCurrentBitIndex;
    }

    public void setNextBoolean(final boolean pBoolean) {
        if (pBoolean) {
            setNextInteger(1, 1);
        } else {
            setNextInteger(0, 1);
        }
    }

    public void setNextByte(final byte[] pValue, final int pLength) {
        setNextByte(pValue, pLength, true);
    }

    public void setNextByte(final byte[] pValue, final int pLength, final boolean pPadBefore) {
        int totalSize = (int) Math.ceil(pLength / BYTE_SIZE_F);
        ByteBuffer buffer = ByteBuffer.allocate(totalSize);
        int size = Math.max(totalSize - pValue.length, 0);
        if (pPadBefore) {
            for (int i = 0; i < size; i++) {
                buffer.put((byte) 0);
            }
        }
        buffer.put(pValue, 0, Math.min(totalSize, pValue.length));
        if (!pPadBefore) {
            for (int i = 0; i < size; i++) {
                buffer.put((byte) 0);
            }
        }
        byte tab[] = buffer.array();
        if (currentBitIndex % BYTE_SIZE != 0) {
            int index = 0;
            int max = currentBitIndex + pLength;
            while (currentBitIndex < max) {
                int mod = currentBitIndex % BYTE_SIZE;
                int modTab = index % BYTE_SIZE;
                int length = Math.min(max - currentBitIndex, Math.min(BYTE_SIZE - mod, BYTE_SIZE - modTab));
                byte val = (byte) (tab[index / BYTE_SIZE] & getMask(modTab, length));
                if (mod == 0) {
                    val = (byte) (val << Math.min(modTab, BYTE_SIZE - length));
                } else {
                    val = (byte) ((val & DEFAULT_VALUE) >> mod);
                }
                byteTab[currentBitIndex / BYTE_SIZE] |= val;
                currentBitIndex += length;
                index += length;
            }

        } else {
            System.arraycopy(tab, 0, byteTab, currentBitIndex / BYTE_SIZE, tab.length);
            currentBitIndex += pLength;
        }
    }

    public void setNextDate(final Date pValue, final String pPattern) {
        setNextDate(pValue, pPattern, false);
    }

    public void setNextDate(final Date pValue, final String pPattern, final boolean pUseBcd) {
        
        SimpleDateFormat sdf = new SimpleDateFormat(pPattern, Locale.US);
        String value = sdf.format(pValue);

        if (pUseBcd) {
            setNextHexaString(value, value.length() * 4);
        } else {
            setNextString(value, value.length() * 8);
        }
    }

    public void setNextHexaString(final String pValue, final int pLength) {
        setNextByte(BytesUtils.fromString(pValue), pLength);
    }

    public void setNextLong(final long pValue, final int pLength) {

        if (pLength > Long.SIZE) {
            throw new IllegalArgumentException("Long overflow with length > 64");
        }

        setNextValue(pValue, pLength, Long.SIZE - 1);
    }

    protected void setNextValue(final long pValue, final int pLength, final int pMaxSize) {
        long value = pValue;
        
        long bitMax = (long) Math.pow(2, Math.min(pLength, pMaxSize));
        if (pValue > bitMax) {
            value = bitMax - 1;
        }
        
        int writeSize = pLength;
        while (writeSize > 0) {
            
            int mod = currentBitIndex % BYTE_SIZE;
            byte ret = 0;
            if (mod == 0 && writeSize <= BYTE_SIZE || pLength < BYTE_SIZE - mod) {
                
                ret = (byte) (value << BYTE_SIZE - (writeSize + mod));
            } else {
                
                long length = Long.toBinaryString(value).length();
                ret = (byte) (value >> writeSize - length - (BYTE_SIZE - length - mod));
            }
            byteTab[currentBitIndex / BYTE_SIZE] |= ret;
            long val = Math.min(writeSize, BYTE_SIZE - mod);
            writeSize -= val;
            currentBitIndex += val;
        }
    }

    public void setNextInteger(final int pValue, final int pLength) {

        if (pLength > Integer.SIZE) {
            throw new IllegalArgumentException("Integer overflow with length > 32");
        }

        setNextValue(pValue, pLength, Integer.SIZE - 1);
    }

    public void setNextString(final String pValue, final int pLength) {
        setNextString(pValue, pLength, true);
    }

    public void setNextString(final String pValue, final int pLength, final boolean pPaddedBefore) {
        setNextByte(pValue.getBytes(Charset.defaultCharset()), pLength, pPaddedBefore);
    }
}
