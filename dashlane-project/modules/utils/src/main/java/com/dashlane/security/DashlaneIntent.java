package com.dashlane.security;

import android.content.Context;
import android.content.Intent;



public class DashlaneIntent {

    private static final String DASHLANE_PACKAGE = "com.dashlane";

    private DashlaneIntent() {
        
    }

    public static Intent newInstance() {
        Intent intent = new Intent();
        return setDashlanePackageName(intent);
    }

    public static Intent newInstance(String action) {
        Intent intent = new Intent(action);
        return setDashlanePackageName(intent);
    }

    public static Intent newInstance(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        return setDashlanePackageName(intent);
    }

    private static Intent setDashlanePackageName(Intent intent) {
        intent.setPackage(DASHLANE_PACKAGE);
        return intent;
    }
}
