package com.dashlane.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;

import com.dashlane.analytics.referrer.ReferrerManager;
import com.dashlane.async.BroadcastManager;
import com.dashlane.authentication.sso.GetUserSsoInfoActivity;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.navigation.NavigationConstants;
import com.dashlane.preference.ConstantsPrefs;
import com.dashlane.preference.GlobalPreferencesManager;
import com.dashlane.session.Username;
import com.dashlane.util.ActivityUtils;
import com.dashlane.util.log.FirstLaunchDetector;
import com.dashlane.util.log.LaunchLogger;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
public class SplashScreenActivity extends FragmentActivity {

    @Inject
    FirstLaunchDetector firstLaunchDetector;

    @Inject
    LaunchLogger launchLogger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        keepSplashScreenTheme();

        if (interceptUserSsoInfo()) {
            finish();
            return;
        }

        SingletonProvider.getDaDaDa().refreshAsync(this);

                .info("SplashScreenActivity onCreate", "", true);

        proceedWithLoading();

        if (savedInstanceState == null) {
            launchLogger.logLaunched();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        firstLaunchDetector.detect();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.setIntent(intent);
        
        
    }

    private void proceedWithLoading() {
        GlobalPreferencesManager preferencesManager = SingletonProvider.getGlobalPreferencesManager();
        BroadcastManager.removeAllBufferedIntent();
        Username lastUser = preferencesManager.getDefaultUsername();
        if (lastUser != null) {
            preferencesManager.saveSkipIntro();
        }
        doMarketingStuff();
        startNextActivity();
    }

    private void startNextActivity() {
        SplashScreenIntentFactory intentFactory = SplashScreenIntentFactory.create(this);
        Intent newIntent = intentFactory.createIntent();
        newIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        Intent currentIntent = getIntent();
        newIntent.putExtra(NavigationConstants.STARTED_WITH_INTENT, currentIntent);
        if (currentIntent != null) {
            final Uri uri = currentIntent.getData();
            if (uri != null) {
                
                startActivity(newIntent);
                finishAffinity();
                return;
            }
        }

        Bundle options = ActivityOptionsCompat.makeCustomAnimation(this,
                android.R.anim.fade_in,
                android.R.anim.fade_out).toBundle();
        startActivity(newIntent, options);
        finishAffinity();
    }

    private void doMarketingStuff() {
        ReferrerManager.getInstance().initialize(
                SingletonProvider.getGlobalPreferencesManager().getString(ConstantsPrefs.REFERRED_BY));
    }


    private boolean interceptUserSsoInfo() {
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data == null || !"ssologin".equals(data.getHost())) {
            return false;
        }

        int flags = intent.getFlags();

        if ((flags & Intent.FLAG_ACTIVITY_NEW_TASK) == 0 ||
                (flags & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) == 0) {
            
            
            
            flags = flags | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT;
            Intent newIntent = new Intent(this, SplashScreenActivity.class)
                    .setData(data)
                    .setFlags(flags);
            startActivity(newIntent);
        } else {
            startActivity(GetUserSsoInfoActivity.createUserSsoInfoHandlingIntent(this, data));
        }
        return true;
    }

    private void keepSplashScreenTheme() {
        final boolean[] started = {false};

        ActivityUtils.findContentParent(this)
                .getViewTreeObserver()
                .addOnPreDrawListener(() -> {
                    if (!started[0]) {
                        started[0] = getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.STARTED);
                    }

                    return started[0];
                });
    }
}
