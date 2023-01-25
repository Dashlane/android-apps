package com.dashlane.core.helpers;



public class DashlaneHelper {
    private static long mStartTime = 0;

    private DashlaneHelper() {
        
    }

    public static void setStartTime(long start) {
        mStartTime = start;
    }

    public static long getTimeSinceLaunch() {
        return (System.currentTimeMillis() - mStartTime) / 1000;
    }
}
