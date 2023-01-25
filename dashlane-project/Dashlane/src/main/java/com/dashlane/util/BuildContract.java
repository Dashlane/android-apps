package com.dashlane.util;

import android.annotation.SuppressLint;

import com.dashlane.BuildConfig;

import java.time.ZonedDateTime;

public class BuildContract {

    private BuildContract() {
        
    }

    @SuppressLint("WrongConstant")
    public static void initializeSystemSettings() {
        try {
            Class.forName("com.dashlane.tests.utilities.robotium.DashlaneSolo");
            if (BuildConfig.DEBUG) {
                Constants.TESTS.IS_TESTING_BUILD = true;
                ZonedDateTime now = ZonedDateTime.now();
                Constants.TESTS.TRACE_LOGS_FILENAME = "traces_"
                                                      + now.getYear() + "-" +
                                                      (now.getMonth()) + "-" + now.getDayOfMonth()
                                                      + "_" + now.getHour()
                                                      + ":" + now.getMinute()
                                                      + ":" + now.getSecond();
                
            }
        } catch (ClassNotFoundException e) {
            Constants.TESTS.IS_TESTING_BUILD = false;
        }

    }
}