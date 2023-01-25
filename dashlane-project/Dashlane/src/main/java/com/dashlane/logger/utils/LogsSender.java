package com.dashlane.logger.utils;

import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.logger.Log;

public class LogsSender {

    private LogsSender() {
        
    }

    public static void flushLogs() {
        Log.d("TRACKING", "Send logs");
        SingletonProvider.getComponent().getUserActivityFlush().invoke();
        SingletonProvider.getComponent().getLogFlush().invoke();
    }
}
