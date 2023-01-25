package com.dashlane.util;

import androidx.annotation.Nullable;
import kotlin.text.Charsets;
import okio.ByteString;



@SuppressWarnings("WeakHashAlgorithm")
public class MD5Hash {
    private MD5Hash() {
    }

    public static String hash(@Nullable String dataToHash) {
        if (dataToHash != null && dataToHash.length() != 0) {
            try {
                return ByteString.encodeString(dataToHash, Charsets.ISO_8859_1).md5().hex();
            } catch (Exception ignored) {
                
            }
        }
        return null;
    }
}
