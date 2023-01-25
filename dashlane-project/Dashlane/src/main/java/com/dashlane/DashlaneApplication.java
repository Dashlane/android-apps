package com.dashlane;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Looper;
import android.webkit.WebView;

import com.braze.BrazeActivityLifecycleCallbackListener;
import com.braze.ui.inappmessage.BrazeInAppMessageManager;
import com.dashlane.autofill.api.changepassword.AutofillApiChangePasswordApplication;
import com.dashlane.autofill.api.changepause.AutofillApiChangePauseApplication;
import com.dashlane.autofill.api.common.AutofillApiGeneratePasswordApplication;
import com.dashlane.autofill.api.createaccount.AutofillApiCreateAccountApplication;
import com.dashlane.autofill.api.internal.AutofillApiApplication;
import com.dashlane.autofill.api.pause.AutofillApiPauseApplication;
import com.dashlane.autofill.api.rememberaccount.AutofillApiRememberAccountApplication;
import com.dashlane.autofill.api.totp.AutofillApiTotpApplication;
import com.dashlane.autofill.api.unlinkaccount.AutofillApiUnlinkAccountsApplication;
import com.dashlane.autofill.api.viewallaccounts.AutofillApiViewAllAccountsApplication;
import com.dashlane.crashreport.CrashReporterComponent;
import com.dashlane.createaccount.component.SessionRetrieverApplication;
import com.dashlane.dagger.singleton.SingletonComponentProxy;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.debug.DeveloperUtilities;
import com.dashlane.device.component.DeviceInfoRepositoryApplication;
import com.dashlane.followupnotification.FollowUpNotificationApplication;
import com.dashlane.guidedonboarding.GuidedOnboardingApplication;
import com.dashlane.hermes.inject.HermesApplication;
import com.dashlane.managers.PerfLogManager;
import com.dashlane.network.inject.RetrofitApplication;
import com.dashlane.premium.current.dagger.CurrentPlanComponent;
import com.dashlane.sharing.SharingKeysHelperApplication;
import com.dashlane.ui.component.EndOfLifeApplication;
import com.dashlane.ui.component.UiPartApplication;
import com.dashlane.ui.menu.MenuComponent;
import com.dashlane.ui.screens.fragments.search.dagger.SearchApplication;
import com.dashlane.url.icon.UrlDomainIconComponent;
import com.dashlane.useractivity.log.inject.UserActivityApplication;
import com.dashlane.util.BuildContract;
import com.dashlane.util.inject.ComponentApplication;
import android.app.Application.ActivityLifecycleCallbacks;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.appcompat.app.AppCompatDelegate;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class DashlaneApplication extends Application implements ComponentApplication,
                                                                SessionRetrieverApplication,
                                                                DeviceInfoRepositoryApplication,
                                                                UiPartApplication,
                                                                RetrofitApplication,
                                                                SharingKeysHelperApplication,
                                                                AutofillApiApplication,
                                                                AutofillApiViewAllAccountsApplication,
                                                                AutofillApiPauseApplication,
                                                                AutofillApiTotpApplication,
                                                                AutofillApiGeneratePasswordApplication,
                                                                AutofillApiCreateAccountApplication,
                                                                AutofillApiChangePasswordApplication,
                                                                AutofillApiRememberAccountApplication,
                                                                AutofillApiChangePauseApplication,
                                                                AutofillApiUnlinkAccountsApplication,
                                                                GuidedOnboardingApplication,
                                                                UserActivityApplication,
                                                                UrlDomainIconComponent.Application,
                                                                CrashReporterComponent.Application,
                                                                MenuComponent.Application,
                                                                HermesApplication,
                                                                FollowUpNotificationApplication,
                                                                CurrentPlanComponent.Application,
                                                                SearchApplication,
                                                                EndOfLifeApplication {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private final ApplicationObserver dashlaneObserver = new DashlaneApplicationObserver();

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
            PerfLogManager.getInstance().onTerminate();
            SingletonProvider.getBreachManager().onTerminate();
        }
        super.onTerminate();
    }

    @NotNull
    @Override
    public SingletonComponentProxy getComponent() {
        return SingletonProvider.getComponent();
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

    interface ApplicationObserver {
        void onCreate(DashlaneApplication application);

        void onTerminate(DashlaneApplication application);
    }
}
