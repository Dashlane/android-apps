package com.dashlane.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.preference.ConstantsPrefs;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;



public class Network {
    private final Context mContext;

    @Inject
    public Network(@ApplicationContext Context context) {
        mContext = context;
    }

    public boolean isOn() {
        return isOn(mContext);
    }

    public static boolean isOn(Context context) {
        switch (getType(context)) {
            case TYPE_WIFI:
                return isOnAux(context);
            default:
                return !(SingletonProvider.getSessionManager().getSession() != null
                         && SingletonProvider.getUserPreferencesManager()
                                             .getBoolean(ConstantsPrefs.SYNC_ONLY_ON_WIFI))
                       && isOnAux(context);
        }

    }

    private static boolean isOnAux(Context context) {
        try {
            ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (conMgr != null) {
                NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
                if (networkInfo != null
                    && networkInfo.isAvailable()
                    && networkInfo.isConnected()) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            ExceptionLog.v(e);
            return false;
        }
    }

    public static NetworkType getType(Context context) {
        ConnectivityManager systemService =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            NetworkInfo networkInfo = systemService.getActiveNetworkInfo();
            if (networkInfo == null) {
                return NetworkType.OFFLINE;
            }
            int connectedNetwork = networkInfo.getType();
            return NetworkType.values()[connectedNetwork];
        } catch (Exception e) {
            return NetworkType.OFFLINE;
        }
    }

    public enum NetworkType {
        MOBILE,
        TYPE_WIFI,
        MMS,
        MOBILE_SUPL,
        MOBILE_DUN,
        MOBILE_HIPRI,
        WIMAX,
        BLUETOOTH,
        DUMMY,
        OFFLINE,
        ETHERNET
    }
}
