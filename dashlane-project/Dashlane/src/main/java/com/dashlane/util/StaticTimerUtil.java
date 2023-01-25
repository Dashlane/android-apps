package com.dashlane.util;



public class StaticTimerUtil {
    private static long sAppAbsoluteStart;
    private static long sSplashScreenShown;
    private static long sLoginButtonInteraction;
    private static long sLoginProcessFinish;
    private static boolean sMeasuringLogin = false;

    private StaticTimerUtil() {
        
    }

    public static void setAppAbsoluteStart(long appAbsoluteStart) {
        sAppAbsoluteStart = appAbsoluteStart;
    }

    public static void setSplashScreenShown(long splashScreenShown) {
        sSplashScreenShown = splashScreenShown;
    }

    public static void setLoginButtonInteraction(long loginButtonInteraction) {
        setMeasuringLogin(true);
        sLoginButtonInteraction = loginButtonInteraction;
    }

    public static void setLoginProcessFinish(long loginProcessFinish) {
        sLoginProcessFinish = loginProcessFinish;
    }

    public static long getTimeForSplash() {
        return sSplashScreenShown - sAppAbsoluteStart;
    }

    public static long getTimeForLogin() {
        return sLoginProcessFinish - sLoginButtonInteraction;
    }

    public static boolean isMeasuringLogin() {
        return sMeasuringLogin;
    }

    public static void setMeasuringLogin(boolean measuringLogin) {
        StaticTimerUtil.sMeasuringLogin = measuringLogin;
    }
}
