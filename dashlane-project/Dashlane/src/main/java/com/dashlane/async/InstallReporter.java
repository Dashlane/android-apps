package com.dashlane.async;

import android.content.Context;
import android.os.Build;
import android.util.ArrayMap;

import com.dashlane.BuildConfig;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.debug.DeveloperUtilities;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.preference.ConstantsPrefs;
import com.dashlane.preference.GlobalPreferencesManager;
import com.dashlane.useractivity.InstallReportSender;
import com.dashlane.useractivity.LogSenderService;
import com.dashlane.useractivity.log.LogSender;
import com.dashlane.util.Constants;
import com.dashlane.util.StringUtils;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;



public class InstallReporter {

    private InstallReporter() {
        
    }

    public static void recordInstallLogToServer(final Context context,
                                                final GlobalPreferencesManager preferencesManager,
                                                final LogSenderService installSenderService) {
        boolean installEventRecordedOnServer = preferencesManager.getBoolean(ConstantsPrefs.INSTALL_EVENT);
        if (!installEventRecordedOnServer) {
            ArrayMap<String, String> params = new ArrayMap<>();
            
            try {
                AdvertisingIdClient.Info advertisingIdInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                params.put("advertisingId", advertisingIdInfo.getId());
                params.put("doNotTrack", Boolean.toString(advertisingIdInfo.isLimitAdTrackingEnabled()));
            } catch (Exception e) {
                ExceptionLog.v(e);
            }
            try {
                params.put("origin", "android");
                params.put("osVersion", Build.VERSION.RELEASE);
                params.put("anonymousDeviceId",
                           SingletonProvider.getComponent().getDeviceInfoRepository().getAnonymousDeviceId());
                params.put("test", Boolean.toString(BuildConfig.DEBUG || DeveloperUtilities.systemIsInDebug(context)));
                params.put("appBuildNumber", String.valueOf(BuildConfig.VERSION_CODE));

                
                String referrer = preferencesManager.getString(Constants.MARKETING.REFFERAL_STRING);

                if (StringUtils.isNotSemanticallyNull(referrer)) {
                    params.put("referrer", referrer);
                }

                
                InstallReportSender reportSender = new InstallReportSender(installSenderService);
                reportSender.sendUserActivityLogs(params, new LogSender.Listener() {
                    @Override
                    public void onSuccess() {
                        preferencesManager.putBoolean(ConstantsPrefs.INSTALL_EVENT, true);
                    }

                    @Override
                    public void onFail() {

                    }
                });
            } catch (Exception e) {
                ExceptionLog.v(e);
            }

        }
    }
}
