package com.dashlane;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.work.Configuration;

import com.braze.ui.inappmessage.BrazeInAppMessageManager;
import com.dashlane.debug.DeveloperUtilities;
import com.dashlane.hermes.LogRepository;
import com.dashlane.util.BuildContract;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class DashlaneApplication extends Application implements Configuration.Provider {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Inject
    ApplicationObserver dashlaneObserver;

    @Inject
    LogRepository logRepository;

    @Inject
    HiltWorkerFactory workerFactory;

    @Override
    public void onCreate() {
        super.onCreate();

        BrazeInAppMessageManager.getInstance().ensureSubscribedToInAppMessageEvents(getApplicationContext());

        updateWebViewDataDirectory();
        if (isMainApplicationThread()) {
            dashlaneObserver.onCreate(this);
        }
    }

    @Override
    public void onTerminate() {
        if (isMainApplicationThread()) {
            dashlaneObserver.onTerminate(this);
        }
        super.onTerminate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        
        
        if (!DeveloperUtilities.isUnitTestMode()) {
            BuildContract.initializeSystemSettings();
        }
    }

    private void updateWebViewDataDirectory() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            
            return;
        }
        String processName = getProcessName();
        if (processName == null || "com.dashlane".equals(processName)) {
            
            return;
        }
        
        
        WebView.setDataDirectorySuffix("webview-" + processName);
    }

    private boolean isMainApplicationThread() {
        Looper mainLooper = Looper.getMainLooper();
        return mainLooper != null && mainLooper.isCurrentThread() && "com.dashlane".equals(getMyProcessName());
    }

    private String getMyProcessName() {
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) return null;
        List<ActivityManager.RunningAppProcessInfo> processInfoList = manager.getRunningAppProcesses();
        if (processInfoList == null) return null;
        for (ActivityManager.RunningAppProcessInfo processInfo : processInfoList) {
            if (processInfo.pid == pid) return processInfo.processName;
        }
        return null;
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(workerFactory)
            .build();
    }
}
