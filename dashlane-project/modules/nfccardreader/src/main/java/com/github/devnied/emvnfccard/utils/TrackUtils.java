package com.github.devnied.emvnfccard.utils;

import com.github.devnied.emvnfccard.model.EmvTrack1;
import com.github.devnied.emvnfccard.model.EmvTrack2;
import com.github.devnied.emvnfccard.model.Service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class TrackUtils {

    public static final String CARD_HOLDER_NAME_SEPARATOR = "/";

    private static final Pattern TRACK2_EQUIVALENT_PATTERN = Pattern.compile("([0-9]{1,19})D([0-9]{4})([0-9]{3})?(.*)");

    private static final Pattern TRACK1_PATTERN = Pattern
            .compile("%?([A-Z])([0-9]{1,19})(\\?[0-9])?\\^([^\\^]{2,26})\\^([0-9]{4}|\\^)([0-9]{3}|\\^)([^\\?]+)\\??");

    public static EmvTrack2 extractTrack2EquivalentData(final byte[] pRawTrack2) {
        EmvTrack2 ret = null;

        if (pRawTrack2 != null) {
            EmvTrack2 track2 = new EmvTrack2();
            track2.setRaw(pRawTrack2);
            String data = BytesUtils.bytesToStringNoSpace(pRawTrack2);
            Matcher m = TRACK2_EQUIVALENT_PATTERN.matcher(data);
            
            if (m.find()) {
                
                track2.setCardNumber(m.group(1));
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyMM", Locale.getDefault());
                try {
                    track2.setExpireDate(DateUtils.truncate(sdf.parse(m.group(2)), Calendar.MONTH));
                } catch (ParseException e) {
                    return ret;
                }
                
                track2.setService(new Service(m.group(3)));
                ret = track2;
            }
        }
        return ret;
    }

    public static EmvTrack1 extractTrack1Data(final byte[] pRawTrack1) {
        EmvTrack1 ret = null;

        if (pRawTrack1 != null) {
            EmvTrack1 track1 = new EmvTrack1();
            track1.setRaw(pRawTrack1);
            Matcher m = TRACK1_PATTERN.matcher(new String(pRawTrack1));
            
            if (m.find()) {
                
                track1.setFormatCode(m.group(1));
                
                track1.setCardNumber(m.group(2));
                
                String[] name = StringUtils.split(m.group(4).trim(), CARD_HOLDER_NAME_SEPARATOR);
                if (name != null && name.length == 2) {
                    track1.setHolderLastname(StringUtils.trimToNull(name[0]));
                    track1.setHolderFirstname(StringUtils.trimToNull(name[1]));
                }
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyMM", Locale.getDefault());
                try {
                    track1.setExpireDate(DateUtils.truncate(sdf.parse(m.group(5)), Calendar.MONTH));
                } catch (ParseException e) {
                    return ret;
                }
                
                track1.setService(new Service(m.group(6)));
                ret = track1;
            }
        }
        return ret;
    }

    private TrackUtils() {
    }

}
