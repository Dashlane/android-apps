package com.dashlane.login;

import static com.dashlane.useractivity.log.usage.UsageLogExtensionKt.getUsageLogCode2SenderFromOrigin;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.lifecycle.ViewModelProvider;

import com.dashlane.R;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.hermes.LogRepository;
import com.dashlane.lock.UnlockEvent;
import com.dashlane.login.dagger.TrackingId;
import com.dashlane.login.lock.LockSetting;
import com.dashlane.login.pages.totp.u2f.NfcServiceDetectorImpl;
import com.dashlane.login.root.LoginDataProvider;
import com.dashlane.login.root.LoginPresenter;
import com.dashlane.login.root.LoginViewProxy;
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactory;
import com.dashlane.login.sso.LoginSsoLogger;
import com.dashlane.preference.UserPreferencesManager;
import com.dashlane.session.SessionCredentialsSaver;
import com.dashlane.session.SessionManager;
import com.dashlane.ui.AutoFillDisablerKt;
import com.dashlane.ui.ScreeenshotEnablerKt;
import com.dashlane.ui.activities.DashlaneActivity;
import com.dashlane.ui.endoflife.EndOfLife;
import com.dashlane.useractivity.log.install.InstallLogCode69;

import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import kotlinx.coroutines.Job;



@AndroidEntryPoint
public class LoginActivity extends DashlaneActivity implements LoginSsoLoggerConfigProvider {

    private static final String LOCK_SETTING = "lock_setting";
    private static final String CURRENT_THEME_RES_ID = "current_theme_res_id";

    public static final String ALLOW_SKIP_EMAIL = "allow_skip_email";
    public static final String SYNC_ERROR = "sync_error";

    @Inject
    NfcServiceDetectorImpl mNfcServiceProvider;
    @Inject
    LoginDataProvider mDataProvider;
    @Inject
    UserPreferencesManager mUserPreferencesManager;
    @Inject
    SessionManager mSessionManager;
    @Inject
    SessionCredentialsSaver mSessionCredentialsSaver;
    @Inject
    ContactSsoAdministratorDialogFactory mContactSsoAdministratorDialogFactory;
    @Inject
    LogRepository mLogRepository;
    @Inject
    @TrackingId
    String trackingId;
    @Inject
    EndOfLife mEndOfLife;
    LoginPresenter mNavigationController;

    private int currentThemeResId;

    @Override
    public boolean getRequireUserUnlock() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LockSetting lockSetting = null;
        if (savedInstanceState != null) {
            lockSetting = savedInstanceState.getParcelable(LOCK_SETTING);
        }
        if (lockSetting == null) {
            lockSetting = LockSetting.buildFrom(getExtras());
        }

        currentThemeResId = savedInstanceState == null
                            ? getAppropriateTheme(lockSetting)
                            : savedInstanceState.getInt(CURRENT_THEME_RES_ID);

        setTheme(currentThemeResId);

        super.onCreate(savedInstanceState);
        AutoFillDisablerKt.disableAutoFill(this);
        setContentView(R.layout.activity_login);
        ScreeenshotEnablerKt.applyScreenshotAllowedFlag(getWindow(), SingletonProvider.getScreenshotPolicy());

        View view = findViewById(R.id.view_login_root_container);
        LoginViewProxy viewProxy = new LoginViewProxy(view);

        boolean allowSkipEmail = this.getIntent().getBooleanExtra(ALLOW_SKIP_EMAIL, false);
        mNavigationController = new LoginPresenter(
                new ViewModelProvider(this),
                getCoroutineContext().get(Job.Key),
                SingletonProvider.getLockManager(),
                mUserPreferencesManager,
                mSessionManager,
                mSessionCredentialsSaver,
                mContactSsoAdministratorDialogFactory,
                allowSkipEmail,
                new LoginLoggerImpl(mLogRepository, lockSetting.getUnlockReason()),
                mEndOfLife
        );
        mDataProvider.lockSetting = lockSetting;
        mNavigationController.setProvider(mDataProvider);
        mNavigationController.setView(viewProxy);
        mNavigationController.onCreate(savedInstanceState);
        getLifecycle().addObserver(mNfcServiceProvider);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mNavigationController.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNavigationController.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        
        if (!NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
            setIntent(intent); 
        }
        mNfcServiceProvider.onNewIntent(intent);
        mNavigationController.onNewIntent();
    }

    @Override
    public void onBackPressed() {
        if (!mNavigationController.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mNavigationController.onSaveInstanceState(outState);
        outState.putParcelable(LOCK_SETTING, mDataProvider.lockSetting);
        outState.putInt(CURRENT_THEME_RES_ID, currentThemeResId);
    }

    private Bundle getExtras() {
        Intent intent = getIntent();
        if (intent == null) {
            return null;
        }
        return intent.getExtras();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mNavigationController.onActivityResult(requestCode, resultCode, data);
    }

    

    @StyleRes
    private static int getAppropriateTheme(LockSetting lockSetting) {
        
        boolean isLockedOut = SingletonProvider.getSessionManager().getSession() == null ||
                lockSetting.getUnlockReason() instanceof UnlockEvent.Reason.AppAccess ||
                lockSetting.getUnlockReason() instanceof UnlockEvent.Reason.PairAuthenticatorApp;

        final int themeResId;

        if (isLockedOut && lockSetting.getShouldThemeAsDialog()) {
            themeResId = R.style.Theme_Dashlane_Login_LockedOut_Dialog;
        } else if (isLockedOut) {
            themeResId = R.style.Theme_Dashlane_Login_LockedOut_TranslucentWindow;
        } else if (lockSetting.getShouldThemeAsDialog()) {
            themeResId = R.style.Theme_Dashlane_Login_Dialog;
        } else {
            themeResId = R.style.Theme_Dashlane_Login_TranslucentWindow;
        }

        return themeResId;
    }

    @NonNull
    @Override
    public LoginSsoLogger.Config getSsoLoggerConfig() {
        return new LoginSsoLogger.Config(
                trackingId,
                InstallLogCode69.Type.LOGIN,
                getIntent().getStringExtra(LockSetting.EXTRA_DOMAIN),
                getUsageLogCode2SenderFromOrigin(getIntent())
        );
    }
}
