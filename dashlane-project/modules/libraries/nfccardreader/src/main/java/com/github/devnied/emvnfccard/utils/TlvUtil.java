package com.github.devnied.emvnfccard.utils;

import com.github.devnied.emvnfccard.exception.TlvException;
import com.github.devnied.emvnfccard.iso7816emv.EmvTags;
import com.github.devnied.emvnfccard.iso7816emv.ITag;
import com.github.devnied.emvnfccard.iso7816emv.TLV;
import com.github.devnied.emvnfccard.iso7816emv.TagAndLength;
import com.github.devnied.emvnfccard.reader.TlvInputStream;

import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class TlvUtil {





    private static ITag searchTagById(final int tagId) {
        return EmvTags.getNotNull(getTagAsBytes(tagId));
    }

    private static TLV getNextTLV(final TlvInputStream stream) {
        TLV tlv = null;
        try {
            int left = stream.available();
            if (left <= 2) {
                return tlv;
            }
            ITag tag = searchTagById(stream.readTag());
            int length = stream.readLength();
            if (stream.available() >= length) {
                tlv = new TLV(tag, length, getLengthAsBytes(length), stream.readValue());
            }
        } catch (Exception e) {
            
        } finally {
            try {
                stream.close();
            } catch (Exception e) {
                
            }
        }
        return tlv;
    }

    public static List<TagAndLength> parseTagAndLength(final byte[] data) {
        List<TagAndLength> tagAndLengthList = new ArrayList<TagAndLength>();
        if (data != null) {
            try (TlvInputStream stream = new TlvInputStream(new ByteArrayInputStream(data))) {
                while (stream.available() > 0) {
                    if (stream.available() < 2) {
                        throw new TlvException("Data length < 2 : " + stream.available());
                    }

                    ITag tag = searchTagById(stream.readTag());
                    int tagValueLength = stream.readLength();

                    tagAndLengthList.add(new TagAndLength(tag, tagValueLength));
                }
            } catch (IOException e) {
                
            }
        }
        return tagAndLengthList;
    }

    public static List<TLV> getlistTLV(final byte[] pData, final ITag... pTag) {
        List<TLV> list = new ArrayList<>();
        try (TlvInputStream stream = new TlvInputStream(new ByteArrayInputStream(pData))) {
            while (stream.available() > 0) {

                TLV tlv = TlvUtil.getNextTLV(stream);
                if (tlv == null) {
                    break;
                }
                if (ArrayUtils.contains(pTag, tlv.getTag())) {
                    list.add(tlv);
                } else if (tlv.getTag().isConstructed()) {
                    list.addAll(TlvUtil.getlistTLV(tlv.getValueBytes(), pTag));
                }
            }
        } catch (IOException e) {
            
        }
        return list;
    }

    public static List<TLV> getlistTLV(final byte[] pData, final ITag pTag, final boolean pAdd) {

        List<TLV> list = new ArrayList<>();
        try (TlvInputStream stream = new TlvInputStream(new ByteArrayInputStream(pData))) {
            while (stream.available() > 0) {

                TLV tlv = TlvUtil.getNextTLV(stream);
                if (tlv == null) {
                    break;
                }
                if (pAdd) {
                    list.add(tlv);
                } else if (tlv.getTag().isConstructed()) {
                    list.addAll(TlvUtil.getlistTLV(tlv.getValueBytes(), pTag, tlv.getTag() == pTag));
                }
            }
        } catch (IOException e) {
            
        }
        return list;
    }

    public static byte[] getValue(final byte[] pData, final ITag... pTag) {
        byte[] ret = null;
        if (pData != null) {
            try (TlvInputStream stream = new TlvInputStream(new ByteArrayInputStream(pData))) {
                while (stream.available() > 0) {
                    TLV tlv = TlvUtil.getNextTLV(stream);
                    if (tlv == null) {
                        break;
                    }
                    if (ArrayUtils.contains(pTag, tlv.getTag())) {
                        return tlv.getValueBytes();
                    } else if (tlv.getTag().isConstructed()) {
                        ret = TlvUtil.getValue(tlv.getValueBytes(), pTag);
                        if (ret != null) {
                            break;
                        }
                    }

                }
            } catch (IOException e) {
                
            }
        }
        return ret;
    }

    public static int getLength(final List<TagAndLength> pList) {
        int ret = 0;
        if (pList != null) {
            for (TagAndLength tl : pList) {
                ret += tl.getLength();
            }
        }
        return ret;
    }

    private static byte[] getTagAsBytes(int tag) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int byteCount = (int) (Math.log(tag) / Math.log(256)) + 1;
        for (int i = 0; i < byteCount; i++) {
            int pos = 8 * (byteCount - i - 1);
            out.write((tag & (0xFF << pos)) >> pos);
        }
        byte[] tagBytes = out.toByteArray();
        switch (getTagClass(tag)) {
            case APPLICATION_CLASS:
                tagBytes[0] |= 0x40;
                break;
            case CONTEXT_SPECIFIC_CLASS:
                tagBytes[0] |= 0x80;
                break;
            case PRIVATE_CLASS:
                tagBytes[0] |= 0xC0;
                break;
            default:
                break;
        }
        if (!isPrimitive(tag)) {
            tagBytes[0] |= 0x20;
        }
        return tagBytes;
    }

    private static int getTagClass(int tag) {
        int i = 3;
        for (; i >= 0; i--) {
            int mask = (0xFF << (8 * i));
            if ((tag & mask) != 0x00) {
                break;
            }
        }
        int msByte = (((tag & (0xFF << (8 * i))) >> (8 * i)) & 0xFF);
        switch (msByte & 0xC0) {
            case 0x00:
                return UNIVERSAL_CLASS;
            case 0x40:
                return APPLICATION_CLASS;
            case 0x80:
                return CONTEXT_SPECIFIC_CLASS;
            case 0xC0:
            default:
                return PRIVATE_CLASS;
        }
    }

    private static boolean isPrimitive(int tag) {
        int i = 3;
        for (; i >= 0; i--) {
            int mask = (0xFF << (8 * i));
            if ((tag & mask) != 0x00) {
                break;
            }
        }
        int msByte = (((tag & (0xFF << (8 * i))) >> (8 * i)) & 0xFF);
        return ((msByte & 0x20) == 0x00);
    }

    private static byte[] getLengthAsBytes(int length) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (length < 0x80) {
            out.write(length);
        } else {
            int byteCount = log(length, 256);
            out.write(0x80 | byteCount);
            for (int i = 0; i < byteCount; i++) {
                int pos = 8 * (byteCount - i - 1);
                out.write((length & (0xFF << pos)) >> pos);
            }
        }
        return out.toByteArray();
    }

    private static int log(int n, int base) {
        int result = 0;
        while (n > 0) {
            n = n / base;
            result++;
        }
        return result;
    }

    private TlvUtil() {
    }

}