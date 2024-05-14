package com.github.devnied.emvnfccard.parser.apdu.annotation;

import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.utils.BytesUtils;

import java.lang.reflect.Field;

public class AnnotationData implements Comparable<AnnotationData>, Cloneable {

    private int size;

    private int index;

    private boolean readHexa;

    private Field field;

    private int dateStandard;

    private String format;

    private ITag tag;

    private boolean skip;

    @Override
    public int compareTo(final AnnotationData pO) {
        return Integer.valueOf(index).compareTo(pO.getIndex());
    }

    @Override
    public boolean equals(final Object pObj) {
        boolean ret = false;
        if (pObj instanceof AnnotationData) {
            ret = index == ((AnnotationData) pObj).getIndex();
        }
        return ret;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int size) {
        this.size = size;
    }

    public int getIndex() {
        return index;
    }

    public boolean isReadHexa() {
        return readHexa;
    }

    public Field getField() {
        return field;
    }

    public int getDateStandard() {
        return dateStandard;
    }

    public String getFormat() {
        return format;
    }

    public void setField(final Field field) {
        this.field = field;
    }

    public ITag getTag() {
        return tag;
    }

    public void initFromAnnotation(final Data pData) {
        dateStandard = pData.dateStandard();
        format = pData.format();
        index = pData.index();
        readHexa = pData.readHexa();
        size = pData.size();
        if (pData.tag() != null) {
            tag = EmvTags.find(BytesUtils.fromString(pData.tag()));
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        AnnotationData data = new AnnotationData();
        data.dateStandard = dateStandard;
        data.field = field;
        data.format = new String(format);
        data.index = index;
        data.readHexa = readHexa;
        data.size = size;
        data.tag = tag;
        return data;
    }

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(final boolean skip) {
        this.skip = skip;
    }

}
