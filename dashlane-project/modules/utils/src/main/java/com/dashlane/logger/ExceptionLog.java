package com.dashlane.logger;

public class ExceptionLog {

    private ExceptionLog() {
    }

    

    public static void v(Throwable e) {
        LoggerKt.v(Log.INSTANCE, e);
    }
}
