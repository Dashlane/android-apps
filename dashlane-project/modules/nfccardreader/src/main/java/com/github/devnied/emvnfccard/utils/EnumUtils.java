

package com.github.devnied.emvnfccard.utils;

import com.github.devnied.emvnfccard.model.enums.IKeyEnum;



public final class EnumUtils {

    

    @SuppressWarnings("unchecked")
    public static <T extends IKeyEnum> T getValue(final int pKey, final Class<T> pClass) {
        for (IKeyEnum val : pClass.getEnumConstants()) {
            if (val.getKey() == pKey) {
                return (T) val;
            }
        }
        return null;
    }

    

    private EnumUtils() {
    }
}
