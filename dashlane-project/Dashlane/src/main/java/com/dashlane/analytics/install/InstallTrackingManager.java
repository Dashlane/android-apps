package com.dashlane.analytics.install;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.dashlane.analytics.referrer.ReferrerManager;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.navigation.NavigationConstants;
import com.dashlane.preference.ConstantsPrefs;
import com.dashlane.preference.GlobalPreferencesManager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;



public class InstallTrackingManager {

    public void installEvent(Context context, Intent intent) {
        GlobalPreferencesManager preferencesManager = SingletonProvider.getGlobalPreferencesManager();
        preferencesManager.putLong(ConstantsPrefs.INSTALLATION_TIMESTAMP, Instant.now().toEpochMilli());
        if (intent != null && intent.getExtras() != null) {
            Bundle intentExtras = intent.getExtras();
            try {
                String referrerExtra = intentExtras.getString(NavigationConstants.INSTALL_REFERRER_EXTRA);
                if (referrerExtra == null) {
                    return;
                }
                String referrerDecoded = URLDecoder.decode(referrerExtra, StandardCharsets.UTF_8.name());
                preferencesManager.putString(ConstantsPrefs.FULL_REFERRER, referrerDecoded);
                if (!referrerDecoded.startsWith("?")) {
                    referrerDecoded = "?" + referrerDecoded;
                }
                Uri dataUri = Uri.parse(referrerDecoded);
                parseReferrerExtra(context, dataUri);
                parseReferrerOrigin(context, dataUri);
            } catch (UnsupportedEncodingException e) {
                ExceptionLog.v(e);
            }
        }
    }

    private void parseReferrerExtra(Context context, Uri uri) {
        if (uri == null || !uri.isHierarchical()) {
            return;
        }
        GlobalPreferencesManager preferencesManager = SingletonProvider.getGlobalPreferencesManager();
        String referrer = uri.getQueryParameter(NavigationConstants.INSTALL_REFERRER_NAME_EXTRA);
        if (referrer != null) {
            preferencesManager.putString(ConstantsPrefs.REFERRED_BY, referrer);
            ReferrerManager.getInstance().initialize(referrer);
        }

    }

    private void parseReferrerOrigin(Context context, Uri dataUri) {
        if (dataUri == null || !dataUri.isHierarchical()) {
            return;
        }
        GlobalPreferencesManager preferencesManager = SingletonProvider.getGlobalPreferencesManager();
        String referrerOriginPackage =
                dataUri.getQueryParameter(NavigationConstants.INSTALL_REFERRER_ORIGIN_PACKAGE_EXTRA);
        if (referrerOriginPackage != null) {
            preferencesManager.putString(ConstantsPrefs.REFERRER_ORIGIN_PACKAGE, referrerOriginPackage);
        }
    }

}
