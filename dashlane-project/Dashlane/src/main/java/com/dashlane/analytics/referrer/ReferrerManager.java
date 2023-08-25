package com.dashlane.analytics.referrer;

import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.preference.ConstantsPrefs;
import com.dashlane.preference.GlobalPreferencesManager;

public class ReferrerManager {

    private static ReferrerManager sInstance;
    private String mReferrerOrigin = null;

    private ReferrerManager() {
    }

    public static synchronized ReferrerManager getInstance() {
        if (sInstance == null) {
            sInstance = new ReferrerManager();
        }
        return sInstance;
    }

    public static void destroyInstance() {
        if (sInstance != null) {
            sInstance = null;
        }
    }

    public void initialize(String referrer) {
        mReferrerOrigin = referrer; 
    }

    public String getReferrerOrigin() {
        return mReferrerOrigin;
    }

    public void accountHasBeenCreated() {
        removeOriginPreferences();
    }

    public void userHasLoggedIn() {
        removeOriginPreferences();
    }

    private void removeOriginPreferences() {
        GlobalPreferencesManager preferencesManager = SingletonProvider.getGlobalPreferencesManager();
        preferencesManager.remove(ConstantsPrefs.INSTALLATION_TIMESTAMP);
        preferencesManager.remove(ConstantsPrefs.REFERRED_BY);
        preferencesManager.remove(ConstantsPrefs.REFERRER_ORIGIN_PACKAGE);
        preferencesManager.remove(ConstantsPrefs.REFERRER_UNIQUE_REF_ID);
    }
}
