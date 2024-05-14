
package com.github.devnied.emvnfccard.utils;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

public final class AtrUtils {

    private static final MultiValuedMap<String, String> MAP = new ArrayListValuedHashMap<String, String>();

    static {
        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;

        try {
            is = AtrUtils.class.getResourceAsStream("/smartcard_list.txt");
            isr = new InputStreamReader(is, CharEncoding.UTF_8);
            br = new BufferedReader(isr);

            int lineNumber = 0;
            String line;
            String currentATR = null;
            while ((line = br.readLine()) != null) {
                ++lineNumber;
                if (line.startsWith("#") || line.trim().length() == 0 ||
                    line.contains("http")) { 
                    continue;
                } else if (line.startsWith("\t") && currentATR != null) {
                    MAP.put(currentATR, line.replace("\t", "").trim());
                } else if (line.startsWith("3")) { 
                    currentATR = StringUtils.deleteWhitespace(line.toUpperCase(Locale.US)).replaceAll("9000$", "");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                br.close();
            } catch (Exception e) {
                
            }
            try {
                isr.close();
            } catch (Exception e) {
                
            }
            try {
                is.close();
            } catch (Exception e) {
                
            }
        }
    }

    public static final Collection<String> getDescription(final String pAtr) {
        Collection<String> ret = null;
        if (StringUtils.isNotBlank(pAtr)) {
            String val = StringUtils.deleteWhitespace(pAtr).toUpperCase(Locale.US);
            for (String key : MAP.keySet()) {
                if (val.matches("^" + key + "$")) {
                    ret = (Collection<String>) MAP.get(key);
                    break;
                }
            }
        }
        return ret;
    }

    public static final Collection<String> getDescriptionFromAts(final String pAts) {
        Collection<String> ret = new ArrayList<String>();
        if (StringUtils.isNotBlank(pAts)) {
            String val = StringUtils.deleteWhitespace(pAts).replaceAll("9000$", "");
            for (String key : MAP.keySet()) {
                int j = val.length() - 1;
                int i = key.length() - 1;
                while (i >= 0) {
                    if (key.charAt(i) == '.' || key.charAt(i) == val.charAt(j)) {
                        j--;
                        i--;
                        if (j < 0) {
                            if (!key.substring(key.length() - val.length(), key.length()).replace(".", "").isEmpty()) {
                                ret.addAll(MAP.get(key));
                            }
                            break;
                        }
                    } else if (j != val.length() - 1) {
                        j = val.length() - 1;
                    } else if (i == key.length() - 1) {
                        break;
                    } else {
                        i--;
                    }
                }
            }
        }
        return ret;
    }

    private AtrUtils() {
    }

}
