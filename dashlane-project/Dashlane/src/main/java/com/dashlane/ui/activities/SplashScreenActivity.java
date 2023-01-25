package com.dashlane.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.dashlane.analytics.referrer.ReferrerManager;
import com.dashlane.async.BroadcastManager;
import com.dashlane.authentication.sso.GetUserSsoInfoActivity;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.managers.PerfLogManager;
import com.dashlane.navigation.NavigationConstants;
import com.dashlane.preference.ConstantsPrefs;
import com.dashlane.preference.GlobalPreferencesManager;
import com.dashlane.session.Username;
import com.dashlane.util.ActivityUtils;
import com.dashlane.util.DeepLinkLogger;
import com.dashlane.util.StaticTimerUtil;
import com.dashlane.util.log.FirstLaunchDetector;
import com.dashlane.util.log.LaunchLogger;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
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

        SingletonProvider.getUserSupportFileLogger().add("SplashScreen Display");

        proceedWithLoading();

        if (savedInstanceState == null) {
            launchLogger.logLaunched();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        StaticTimerUtil.setSplashScreenShown(System.currentTimeMillis());
        PerfLogManager.getInstance().sendSplashScreenLoadTimeLog();
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
                DeepLinkLogger.Companion.invoke().log(uri, "attempt");

                
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

        initializeTrackersUsingPlaystoreAndTrackAsync(SingletonProvider.getContext());
    }

    private void initializeTrackersUsingPlaystoreAndTrackAsync(final Context context) {
        
        new Thread(() -> initializeTrackersUsingPlaystoreAndTrack(context)).start();
    }

    private void initializeTrackersUsingPlaystoreAndTrack(Context context) {
        
        if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            try {
                AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo
                        (getApplicationContext());
            } catch (IOException e) {
                
                

            } catch (GooglePlayServicesNotAvailableException e) {
                

            } catch (GooglePlayServicesRepairableException e) {
                

            } catch (NullPointerException e) {
                
            }
        }
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
