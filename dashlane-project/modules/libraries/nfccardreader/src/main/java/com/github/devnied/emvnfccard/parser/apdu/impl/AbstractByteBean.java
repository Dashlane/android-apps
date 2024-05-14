package com.github.devnied.emvnfccard.parser.apdu.impl;

import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.model.AbstractData;
import com.github.devnied.emvnfccard.parser.apdu.IFile;
import com.github.devnied.emvnfccard.parser.apdu.annotation.AnnotationData;
import com.github.devnied.emvnfccard.parser.apdu.annotation.AnnotationUtils;
import com.github.devnied.emvnfccard.utils.BitUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public abstract class AbstractByteBean<T> extends AbstractData implements IFile {

    private static final long serialVersionUID = -2016039522844322383L;

    private Collection<AnnotationData> getAnnotationSet(final Collection<TagAndLength> pTags) {
        Collection<AnnotationData> ret = null;
        if (pTags != null) {
            Map<ITag, AnnotationData> data = AnnotationUtils.getInstance().getMap().get(getClass().getName());
            ret = new ArrayList<AnnotationData>(data.size());
            for (TagAndLength tal : pTags) {
                AnnotationData ann = data.get(tal.getTag());
                if (ann != null) {
                    ann.setSize(tal.getLength() * BitUtils.BYTE_SIZE);
                } else {
                    ann = new AnnotationData();
                    ann.setSkip(true);
                    ann.setSize(tal.getLength() * BitUtils.BYTE_SIZE);
                }
                ret.add(ann);
            }
        } else {
            ret = AnnotationUtils.getInstance().getMapSet().get(getClass().getName());
        }
        return ret;
    }

    @Override
    public void parse(final byte[] pData, final Collection<TagAndLength> pTags, Calendar pNow) {
        Collection<AnnotationData> set = getAnnotationSet(pTags);
        BitUtils bit = new BitUtils(pData);
        Iterator<AnnotationData> it = set.iterator();
        while (it.hasNext()) {
            AnnotationData data = it.next();
            if (data.isSkip()) {
                bit.addCurrentBitIndex(data.getSize());
            } else {
                Object obj = DataFactory.getObject(data, bit, pNow);
                setField(data.getField(), this, obj);
            }
        }
    }

    protected void setField(final Field field, final IFile pData, final Object pValue) {
        if (field != null) {
            try {
                field.set(pData, pValue);
            } catch (Exception e) {
                
            }
        }
    }
}
