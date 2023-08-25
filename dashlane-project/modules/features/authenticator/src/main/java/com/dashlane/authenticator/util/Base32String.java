
package com.dashlane.authenticator.util;

import java.util.HashMap;
import java.util.Locale;

public class Base32String {
    

    private static final Base32String INSTANCE =
            new Base32String("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"); 
    private static final String SEPARATOR = "-";

    static Base32String getInstance() {
        return INSTANCE;
    }

    private final int mMask;
    private final int mShift;
    private final HashMap<Character, Integer> mCharMap;

    protected Base32String(String alphabet) {
        
        char[] digits = alphabet.toCharArray();
        mMask = digits.length - 1;
        mShift = Integer.numberOfTrailingZeros(digits.length);
        mCharMap = new HashMap<>();
        for (int i = 0; i < digits.length; i++) {
            mCharMap.put(digits[i], i);
        }
    }

    public static byte[] decode(String encoded) throws DecodingException {
        return getInstance().decodeInternal(encoded);
    }

    protected byte[] decodeInternal(String encoded) throws DecodingException {
        
        encoded = encoded.trim().replace(SEPARATOR, "").replace(" ", "");

        
        
        
        encoded = encoded.replaceFirst("[=]*$", "");

        
        encoded = encoded.toUpperCase(Locale.US);
        if (encoded.length() == 0) {
            return new byte[0];
        }
        int encodedLength = encoded.length();
        int outLength = encodedLength * mShift / 8;
        byte[] result = new byte[outLength];
        int buffer = 0;
        int next = 0;
        int bitsLeft = 0;
        for (char c : encoded.toCharArray()) {
            if (!mCharMap.containsKey(c)) {
                throw new DecodingException("Illegal character: " + c);
            }
            buffer <<= mShift;
            buffer |= mCharMap.get(c) & mMask;
            bitsLeft += mShift;
            if (bitsLeft >= 8) {
                result[next++] = (byte) (buffer >> (bitsLeft - 8));
                bitsLeft -= 8;
            }
        }
        
        return result;
    }

    public static class DecodingException extends Exception {
        public DecodingException(String message) {
            super(message);
        }
    }
}