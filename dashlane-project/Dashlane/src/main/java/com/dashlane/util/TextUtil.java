package com.dashlane.util;

import static com.dashlane.util.LocaleUtilKt.getOsLang;

public class TextUtil {

    private TextUtil() {
        
    }

    public static String generateRefferalUrl(String refid) {
        StringBuilder builder = new StringBuilder();
        String lang = getOsLang();
        if (!("en").equals(lang) && !"fr".equals(lang)) {
            lang = "en";
        }
        builder.append("https://www.dashlane.com/")
               .append(lang).append("/").append("as/")
               .append(refid);
        return builder.toString();
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

            
            int result;
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
