package com.github.devnied.emvnfccard.utils;

import java.math.BigInteger;

public final class BytesUtils {

    private static final int MAX_BIT_INTEGER = 31;

    private static final int HEXA = 16;

    private static final int LEFT_MASK = 0xF0;

    private static final int RIGHT_MASK = 0xF;

    private static final int CHAR_DIGIT_ZERO = 0x30;

    private static final int CHAR_DIGIT_SEVEN = 0x37;

    private static final char CHAR_SPACE = (char) 0x20;

    public static int byteArrayToInt(final byte[] byteArray) {
        if (byteArray == null) {
            throw new IllegalArgumentException("Parameter 'byteArray' cannot be null");
        }
        return byteArrayToInt(byteArray, 0, byteArray.length);
    }

    public static int byteArrayToInt(final byte[] byteArray, final int startPos, final int length) {
        if (byteArray == null) {
            throw new IllegalArgumentException("Parameter 'byteArray' cannot be null");
        }
        if (length <= 0 || length > 4) {
            throw new IllegalArgumentException("Length must be between 1 and 4. Length = " + length);
        }
        if (startPos < 0 || byteArray.length < startPos + length) {
            throw new IllegalArgumentException("Length or startPos not valid");
        }
        int value = 0;
        for (int i = 0; i < length; i++) {
            value += (byteArray[startPos + i] & 0xFF) << 8 * (length - i - 1);
        }
        return value;
    }

    public static String bytesToString(final byte[] pBytes) {
        return formatByte(pBytes, true, false);
    }

    public static String bytesToString(final byte[] pBytes, final boolean pTruncate) {
        return formatByte(pBytes, true, pTruncate);
    }

    public static String bytesToStringNoSpace(final byte pByte) {
        return formatByte(new byte[] { pByte }, false, false);
    }

    public static String bytesToStringNoSpace(final byte[] pBytes) {
        return formatByte(pBytes, false, false);
    }

    public static String bytesToStringNoSpace(final byte[] pBytes, final boolean pTruncate) {
        return formatByte(pBytes, false, pTruncate);
    }

    private static String formatByte(final byte[] pByte, final boolean pSpace, final boolean pTruncate) {
        String result;
        if (pByte == null) {
            result = "";
        } else {
            int i = 0;
            if (pTruncate) {
                while (i < pByte.length && pByte[i] == 0) {
                    i++;
                }
            }
            if (i < pByte.length) {
                int sizeMultiplier = pSpace ? 3 : 2;
                char[] c = new char[(pByte.length - i) * sizeMultiplier];
                byte b;
                for (int j = 0; i < pByte.length; i++, j++) {
                    b = (byte) ((pByte[i] & LEFT_MASK) >> 4);
                    c[j] = (char) (b > 9 ? b + CHAR_DIGIT_SEVEN : b + CHAR_DIGIT_ZERO);
                    b = (byte) (pByte[i] & RIGHT_MASK);
                    c[++j] = (char) (b > 9 ? b + CHAR_DIGIT_SEVEN : b + CHAR_DIGIT_ZERO);
                    if (pSpace) {
                        c[++j] = CHAR_SPACE;
                    }
                }
                result = pSpace ? new String(c, 0, c.length - 1) : new String(c);
            } else {
                result = "";
            }
        }
        return result;
    }

    public static byte[] fromString(final String pData) {
        if (pData == null) {
            throw new IllegalArgumentException("Argument can't be null");
        }
        StringBuilder sb = new StringBuilder(pData);
        int j = 0;
        for (int i = 0; i < sb.length(); i++) {
            if (!Character.isWhitespace(sb.charAt(i))) {
                sb.setCharAt(j++, sb.charAt(i));
            }
        }
        sb.delete(j, sb.length());
        if (sb.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex binary needs to be even-length :" + pData);
        }
        byte[] result = new byte[sb.length() / 2];
        j = 0;
        for (int i = 0; i < sb.length(); i += 2) {
            result[j++] = (byte) ((Character.digit(sb.charAt(i), 16) << 4) + Character.digit(sb.charAt(i + 1), 16));
        }
        return result;
    }

    public static boolean matchBitByBitIndex(final int pVal, final int pBitIndex) {
        if (pBitIndex < 0 || pBitIndex > MAX_BIT_INTEGER) {
            throw new IllegalArgumentException(
                    "parameter 'pBitIndex' must be between 0 and 31. pBitIndex=" + pBitIndex);
        }
        return (pVal & 1 << pBitIndex) != 0;
    }

    public static byte setBit(final byte pData, final int pBitIndex, final boolean pOn) {
        if (pBitIndex < 0 || pBitIndex > 7) {
            throw new IllegalArgumentException("parameter 'pBitIndex' must be between 0 and 7. pBitIndex=" + pBitIndex);
        }
        byte ret = pData;
        if (pOn) { 
            ret |= 1 << pBitIndex;
        } else { 
            ret &= ~(1 << pBitIndex);
        }
        return ret;
    }

    public static String toBinary(final byte[] pBytes) {
        String ret = null;
        if (pBytes != null && pBytes.length > 0) {
            BigInteger val = new BigInteger(bytesToStringNoSpace(pBytes), HEXA);
            StringBuilder build = new StringBuilder(val.toString(2));
            
            for (int i = build.length(); i < pBytes.length * BitUtils.BYTE_SIZE; i++) {
                build.insert(0, 0);
            }
            ret = build.toString();
        }
        return ret;
    }

    public static byte[] toByteArray(final int value) {
        return new byte[] { 
                            (byte) (value >> 24), 
                            (byte) (value >> 16), 
                            (byte) (value >> 8), 
                            (byte) value 
        };
    }

    private BytesUtils() {
    }
}
