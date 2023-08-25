
package com.dashlane.authenticator.util;

import androidx.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

public class PasscodeGenerator {
    private static final int MAX_PASSCODE_LENGTH = 9;

    private static final int[] DIGITS_POWER
            
            = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

    private final Signer signer;
    private final int codeLength;

    public interface Signer {
        byte[] sign(byte[] data) throws GeneralSecurityException;
    }

    public PasscodeGenerator(@NonNull Signer signer, int passCodeLength) {
        if ((passCodeLength < 0) || (passCodeLength > MAX_PASSCODE_LENGTH)) {
            throw new IllegalArgumentException(
                    "PassCodeLength must be between 1 and " + MAX_PASSCODE_LENGTH
                    + " digits.");
        }
        this.signer = signer;
        this.codeLength = passCodeLength;
    }

    private String padOutput(int value) {
        String result = Integer.toString(value);
        for (int i = result.length(); i < codeLength; i++) {
            result = "0" + result;
        }
        return result;
    }

    public String generateResponseCode(long state)
            throws GeneralSecurityException {
        byte[] value = ByteBuffer.allocate(8).putLong(state).array();
        return generateResponseCode(value);
    }

    public String generateResponseCode(byte[] challenge)
            throws GeneralSecurityException {
        byte[] hash = signer.sign(challenge);

        
        
        int offset = hash[hash.length - 1] & 0xF;
        
        int truncatedHash = hashToInt(hash, offset) & 0x7FFFFFFF;
        int pinValue = truncatedHash % DIGITS_POWER[codeLength];
        return padOutput(pinValue);
    }

    private int hashToInt(byte[] bytes, int start) {
        DataInput input = new DataInputStream(
                new ByteArrayInputStream(bytes, start, bytes.length - start));
        int val;
        try {
            val = input.readInt();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return val;
    }

}