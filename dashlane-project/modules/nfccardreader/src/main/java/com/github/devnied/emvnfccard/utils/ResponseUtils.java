

package com.github.devnied.emvnfccard.utils;

import com.github.devnied.emvnfccard.enums.SwEnum;

import org.apache.commons.lang3.ArrayUtils;



public final class ResponseUtils {

    

    public static boolean isSucceed(final byte[] pByte) {
        return contains(pByte, SwEnum.SW_9000);
    }

    

    public static boolean isEquals(final byte[] pByte, final SwEnum pEnum) {
        return contains(pByte, pEnum);
    }

    

    public static boolean contains(final byte[] pByte, final SwEnum... pEnum) {
        SwEnum val = SwEnum.getSW(pByte);
        return val != null && ArrayUtils.contains(pEnum, val);
    }

    

    private ResponseUtils() {
    }

}
