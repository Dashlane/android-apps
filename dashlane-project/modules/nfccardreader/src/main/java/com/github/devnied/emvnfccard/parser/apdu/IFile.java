package com.github.devnied.emvnfccard.parser.apdu;

import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;

import java.util.Calendar;
import java.util.Collection;

public interface IFile {

    void parse(final byte[] pData, final Collection<TagAndLength> pList, Calendar pNow);

}
