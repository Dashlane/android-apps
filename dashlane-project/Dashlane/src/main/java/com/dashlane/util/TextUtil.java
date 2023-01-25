package com.dashlane.util;

import android.content.Context;

import com.dashlane.dagger.singleton.SingletonProvider;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import androidx.annotation.StringRes;



public class TextUtil {

    private TextUtil() {
        
    }

    public static boolean isServerResponsePositive(String response) {
        if (response == null) {
            return false;
        }
        final String chunk = response.toLowerCase(Locale.US);
        if (chunk.contains("invalid"))
            return false;
        if (chunk.contains("yes"))
            return true;
        if (chunk.contains("success"))
            return true;
        if (chunk.contains("valid"))
            return true;
        if (chunk.contains("\"code\":200,\"message\":\"OK\""))
            return true;
        return chunk.contains("\"message\":\"ok\"");
    }

    public static String generateRefferalUrl(String refid) {
        StringBuilder builder = new StringBuilder();
        String lang = Constants.getOSLang();
        if (!lang.equals("en") || !lang.equals("fr")) {
            lang = "en";
        }
        builder.append(Constants.HTTP.RefferalUrlHeader)
               .append(lang).append("/").append("as/")
               .append(refid);
        return builder.toString();
    }

    public static String formatDate(LocalDate localDate, @StringRes int dateFormat) {
        Context context = SingletonProvider.getContext();
        Locale locale = context.getResources().getConfiguration().getLocales().get(0);

        try {
            DateTimeFormatter dateTimeFormatter =
                    DateTimeFormatter.ofPattern(context.getString(dateFormat), locale);
            return dateTimeFormatter.format(localDate);
        } catch (Exception e) {
            return null;
        }
    }

    public static long daysRemaining(Instant endExclusive) {
        Duration duration = Duration.between(Instant.now(), endExclusive);
        if (duration.isNegative()) {
            return 0;
        } else {
            return duration.toDays();
        }
    }

    public static String getStringFromUTF8Bytes(byte[] data) {
        if (data == null) {
            return null;
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("squid:S2245")
    public static String generateRandomString(int length, String alphabet) {
        if (length < 1) {
            throw new IllegalArgumentException("Length must be >= 1");
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; ++i) {
            
            builder.append(alphabet.charAt((int) (Math.random() * alphabet.length())));
        }
        return builder.toString();
    }

    public static int compare(String str1, String str2) {
        if (str1 == null) {
            if (str2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else if (str2 == null) {
            return 1;
        } else {
            return str1.compareToIgnoreCase(str2);
        }
    }

    

    
    @SuppressWarnings("squid:S3776")
    public static int compareAlphaNumeric(String s1, String s2) {

        int thisMarker = 0;
        int thatMarker = 0;
        int s1Length = s1 == null ? 0 : s1.length();
        int s2Length = s2 == null ? 0 : s2.length();

        while (thisMarker < s1Length && thatMarker < s2Length) {
            String thisChunk = getChunk(s1, s1Length, thisMarker);
            thisMarker += thisChunk.length();

            String thatChunk = getChunk(s2, s2Length, thatMarker);
            thatMarker += thatChunk.length();

            
            int result = 0;
            if (isDigit(thisChunk.charAt(0)) && isDigit(thatChunk.charAt(0))) {
                
                int thisChunkLength = thisChunk.length();
                result = thisChunkLength - thatChunk.length();
                
                if (result == 0) {
                    for (int i = 0; i < thisChunkLength; i++) {
                        result = thisChunk.charAt(i) - thatChunk.charAt(i);
                        if (result != 0) {
                            return result;
                        }
                    }
                }
            } else {
                result = thisChunk.compareToIgnoreCase(thatChunk);
            }

            if (result != 0)
                return result;
        }

        return s1Length - s2Length;
    }

    

    public static int sumStringByChar(String target) {
        if (!StringUtils.isNotSemanticallyNull(target)) {
            return 0;
        }
        int sum = 0;
        for (int i = 0; i < target.length(); i++) {
            sum += target.charAt(i);
        }
        return sum;
    }

    private static boolean isDigit(char ch) {
        return ch >= 48 && ch <= 57;
    }

    

    private static String getChunk(String s, int slength, int marker) {
        StringBuilder chunk = new StringBuilder();
        char c = s.charAt(marker);
        chunk.append(c);
        marker++;
        if (isDigit(c)) {
            while (marker < slength) {
                c = s.charAt(marker);
                if (!isDigit(c))
                    break;
                chunk.append(c);
                marker++;
            }
        } else {
            while (marker < slength) {
                c = s.charAt(marker);
                if (isDigit(c))
                    break;
                chunk.append(c);
                marker++;
            }
        }
        return chunk.toString();
    }
}
