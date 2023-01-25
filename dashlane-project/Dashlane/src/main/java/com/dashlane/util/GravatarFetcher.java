package com.dashlane.util;

import androidx.annotation.Nullable;



public class GravatarFetcher {

    private static final String GRAVATAR_HEADER = "https://www.gravatar.com/avatar/";
    private static final String GRAVATAR_FOOTER = "?s=200&r=pg&d=404";

    private GravatarFetcher() {
        
    }

    public static String generateGravatarUrl(@Nullable String email) {
        if (!StringUtils.isNotSemanticallyNull(email)) {
            return null;
        }
        return GRAVATAR_HEADER + MD5Hash.hash(email) + GRAVATAR_FOOTER;
    }

}
