

package com.github.devnied.emvnfccard.utils;

import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.iso7816emv.impl.TagImpl;
import com.github.devnied.emvnfccard.model.CPLC;




public final class CPLCUtils {

    

    private static final ITag CPLC_TAG =
            new TagImpl("9f7f", TagValueTypeEnum.BINARY, "Card Production Life Cycle Data", "");

    

    public static CPLC parse(byte[] raw) {
        CPLC ret = null;
        if (raw != null) {
            byte[] cplc = null;
            
            if (raw.length == CPLC.SIZE + 2) {
                cplc = raw;
            }
            
            else if (raw.length == CPLC.SIZE + 5) {
                cplc = TlvUtil.getValue(raw, CPLC_TAG);
            } else {
                return null;
            }
            ret = new CPLC();
            ret.parse(cplc, null);
        }
        return ret;
    }

    

    private CPLCUtils() {
    }
}