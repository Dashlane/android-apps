package com.dashlane.crashreport;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dashlane.BuildConfig;
import com.dashlane.crashreport.reporter.SentryCrashReporter;
import com.dashlane.preference.GlobalPreferencesManager;
import com.dashlane.util.userfeatures.UserFeaturesChecker;

import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class CrashReporterManager implements CrashReporter {

    private static final String PREF_CRASH_DEVICE_ID = "pref_crash_device_id";
    private final List<Client> mCrashReporters = new ArrayList<>();

    private final CrashTrace mCrashTrace = new CrashTrace(this);
    private final CrashReporterLogger mCrashReporterLogger;
    private final GlobalPreferencesManager mGlobalPreferencesManager;
    private final Context mContext;
    private final UserFeaturesChecker mUserFeaturesChecker;

    @Inject
    public CrashReporterManager(CrashReporterLogger crashReporterLogger,
                                GlobalPreferencesManager preferencesManager,
                                @ApplicationContext Context context,
                                UserFeaturesChecker userFeaturesChecker) {
        mCrashReporterLogger = crashReporterLogger;
        mGlobalPreferencesManager = preferencesManager;
        mContext = context;
        mUserFeaturesChecker = userFeaturesChecker;
    }

    @Override
    public void init(@NonNull Application application) {
        if (BuildConfig.DEBUG) {
            return; 
        }
        String crashDeviceId = getCrashReporterId();
        mCrashTrace.autoTrackActivities(application);
        mCrashReporters.clear();

        addSentry();

        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            mCrashReporterLogger.onCrashHappened(thread.toString(), throwable);
            if (!BuildConfig.CRASHLYTICS_ENABLED) {
                emailCrashReport(application, crashDeviceId, throwable);
            }
            
            if (defaultHandler != null) defaultHandler.uncaughtException(thread, throwable);
        });

    }

    private void addSentry() {
        boolean isMissing = true;
        for (int i = 0; isMissing && i < mCrashReporters.size(); i++) {
            if (mCrashReporters.get(i) instanceof SentryCrashReporter) {
                isMissing = false;
            }
        }
        if (isMissing) {
            mCrashReporters.add(new SentryCrashReporter(mContext, getCrashReporterId(), mUserFeaturesChecker));
        }
    }

    private void emailCrashReport(@NonNull Application application, String deviceId, Throwable throwable) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Dashlane Crashed");

        StringWriter writer = new StringWriter();
        writer.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n");
        writer.append("Model: ").append(Build.MODEL).append("\n");
        writer.append("Crash Reporter Id: ").append(deviceId).append("\n");
        writer.append("App Version Name: ").append(BuildConfig.VERSION_NAME).append("\n");
        writer.append("App Version Code: ").append(String.valueOf(BuildConfig.VERSION_CODE)).append("\n");
        writer.append("OS Version: ").append(String.valueOf(Build.VERSION.SDK_INT)).append("\n\n");
        writer.append("StackTrace: ").append("\n");
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        writer.append("\n\nCause:");
        Throwable cause = throwable.getCause();
        while (cause != null) {
            writer.append("\n");
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        intent.putExtra(Intent.EXTRA_TEXT, writer.toString());

        if (intent.resolveActivity(application.getPackageManager()) != null) {
            Intent chooserIntent = Intent.createChooser(intent, "Send Dashlane Crash");
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            application.startActivity(chooserIntent);
        }
    }

    @Override
    public void logNonFatal(@NonNull Throwable throwable) {
        for (int i = 0; i < mCrashReporters.size(); i++) {
            mCrashReporters.get(i).logException(throwable);
        }
    }

    @Override
    public void addInformation(@NotNull String rawTrace) {
        mCrashTrace.add(rawTrace);
    }

    @Override
    public void addInformation(@NonNull String traceWithDate, @NonNull String rawTrace) {
        for (int i = 0; i < mCrashReporters.size(); i++) {
            mCrashReporters.get(i).log(traceWithDate, rawTrace);
        }
    }

    @Override
    public void addLifecycleInformation(@Nullable Fragment fragment, @NonNull String cycle) {
        mCrashTrace.add(fragment, cycle);
    }

    @NonNull
    @Override
    public String getCrashReporterId() {
        String deviceId = mGlobalPreferencesManager.getString(PREF_CRASH_DEVICE_ID);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            mGlobalPreferencesManager.putString(PREF_CRASH_DEVICE_ID, deviceId);
        }
        return deviceId;
    }

    public interface Client {

        void log(@NonNull String traceWithDate, @NonNull String rawTrace);

        void logException(@NonNull Throwable throwable);
    }

}
