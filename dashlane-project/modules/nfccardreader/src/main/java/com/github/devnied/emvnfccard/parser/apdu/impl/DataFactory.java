package com.github.devnied.emvnfccard.parser.apdu.impl;

import android.annotation.SuppressLint;

import com.github.devnied.emvnfccard.model.enums.IKeyEnum;
import com.github.devnied.emvnfccard.parser.apdu.annotation.AnnotationData;
import com.github.devnied.emvnfccard.utils.BitUtils;
import com.github.devnied.emvnfccard.utils.EnumUtils;

import java.util.Calendar;
import java.util.Date;

public final class DataFactory {

    public static final int BCD_DATE = 1;

    public static final int CPCL_DATE = 2;

    public static final int HALF_BYTE_SIZE = 4;

    public static final String BCD_FORMAT = "BCD_Format";

    private static Date getDate(final AnnotationData pAnnotation, final BitUtils pBit, Calendar pNow) {
        Date date = null;
        if (pAnnotation.getDateStandard() == BCD_DATE) {
            date = pBit.getNextDate(pAnnotation.getSize(), pAnnotation.getFormat(), true);
        } else if (pAnnotation.getDateStandard() == CPCL_DATE) {
            date = calculateCplcDate(pBit.getNextByte(pAnnotation.getSize()), pNow);
        } else {
            date = pBit.getNextDate(pAnnotation.getSize(), pAnnotation.getFormat());
        }
        return date;
    }


    @SuppressLint("JavaUtilDateUsage")
    public static Date calculateCplcDate(byte[] dateBytes, Calendar now)
            throws IllegalArgumentException {
        if (dateBytes == null || dateBytes.length != 2) {
            throw new IllegalArgumentException(
                    "Error! CLCP Date values consist always of exactly 2 bytes");
        }
        
        if (dateBytes[0] == 0 && dateBytes[1] == 0) {
            return null;
        }

        int currenctYear = now.get(Calendar.YEAR);
        int startYearOfCurrentDecade = currenctYear - (currenctYear % 10);

        int days = 100 * (dateBytes[0] & 0xF) + 10 * (0xF & dateBytes[1] >>> 4) + (dateBytes[1] & 0xF);

        if (days > 366) {
            throw new IllegalArgumentException(
                    "Invalid date (or are we parsing it wrong??)");
        }

        Calendar calculatedDate = Calendar.getInstance();
        calculatedDate.clear();
        int year = startYearOfCurrentDecade + (0xF & dateBytes[0] >>> 4);
        calculatedDate.set(Calendar.YEAR, year);
        calculatedDate.set(Calendar.DAY_OF_YEAR, days);
        while (calculatedDate.after(now)) {
            year = year - 10;
            calculatedDate.clear();
            calculatedDate.set(Calendar.YEAR, year);
            calculatedDate.set(Calendar.DAY_OF_YEAR, days);
        }
        return calculatedDate.getTime();
    }

    private static int getInteger(final AnnotationData pAnnotation, final BitUtils pBit) {
        return pBit.getNextInteger(pAnnotation.getSize());
    }

    public static Object getObject(final AnnotationData pAnnotation, final BitUtils pBit, Calendar pNow) {
        Object obj = null;
        Class<?> clazz = pAnnotation.getField().getType();

        if (clazz.equals(Integer.class)) {
            obj = getInteger(pAnnotation, pBit);
        } else if (clazz.equals(Float.class)) {
            obj = getFloat(pAnnotation, pBit);
        } else if (clazz.equals(String.class)) {
            obj = getString(pAnnotation, pBit);
        } else if (clazz.equals(Date.class)) {
            obj = getDate(pAnnotation, pBit, pNow);
        } else if (clazz.isEnum()) {
            obj = getEnum(pAnnotation, pBit);
        }
        return obj;
    }

    private static Float getFloat(final AnnotationData pAnnotation, final BitUtils pBit) {
        Float ret = null;

        if (BCD_FORMAT.equals(pAnnotation.getFormat())) {
            ret = Float.parseFloat(pBit.getNextHexaString(pAnnotation.getSize()));
        } else {
            ret = (float) getInteger(pAnnotation, pBit);
        }

        return ret;
    }

    @SuppressWarnings("unchecked")
    private static IKeyEnum getEnum(final AnnotationData pAnnotation, final BitUtils pBit) {
        int val = 0;
        try {
            val = Integer.parseInt(pBit.getNextHexaString(pAnnotation.getSize()), pAnnotation.isReadHexa() ? 16 : 10);
        } catch (NumberFormatException nfe) {
            
        }
        return EnumUtils.getValue(val, (Class<? extends IKeyEnum>) pAnnotation.getField().getType());
    }

    private static String getString(final AnnotationData pAnnotation, final BitUtils pBit) {
        String obj = null;

        if (pAnnotation.isReadHexa()) {
            obj = pBit.getNextHexaString(pAnnotation.getSize());
        } else {
            obj = pBit.getNextString(pAnnotation.getSize()).trim();
        }

        return obj;
    }

    private DataFactory() {
    }
}
