package com.dashlane.masterpassword;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.dashlane.R;
import com.dashlane.abtesting.RemoteAbTestManager;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.hermes.generated.definitions.AnyPage;
import com.dashlane.hermes.LogRepository;
import com.dashlane.login.lock.LockManager;
import com.dashlane.masterpassword.logger.ChangeMasterPasswordLogger;
import com.dashlane.passwordstrength.PasswordStrengthEvaluator;
import com.dashlane.session.BySessionRepository;
import com.dashlane.session.SessionManager;
import com.dashlane.ui.activities.DashlaneActivity;
import com.dashlane.useractivity.log.usage.UsageLogRepository;
import com.dashlane.util.PageViewUtil;

import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwnerKt;

public class ChangeMasterPasswordActivity extends DashlaneActivity {

    private static final String EXTRA_ORIGIN = "origin";
    private static final String EXTRA_WARNING_DESKTOP_SHOWN = "warning_desktop_shown";

    @Inject
    ChangeMasterPasswordContract.DataProvider mDataProvider;

    @Inject
    ChangeMasterPasswordFeatureAccessChecker mChangeMasterPasswordFeatureAccessChecker;

    @Inject
    PasswordStrengthEvaluator mPasswordStrengthEvaluator;

    @Inject
    LockManager lockManager;

    @Inject
    RemoteAbTestManager mRemoteAbTestManager;

    @Inject
    ChangeMasterPasswordLogoutHelper mChangeMasterPasswordLogoutHelper;

    @Inject
    SessionManager mSessionManager;

    @Inject
    BySessionRepository<UsageLogRepository> mBySessionUsageLogRepository;

    @Inject
    LogRepository mLogRepository;

    private ChangeMasterPasswordPresenter presenter = null;

    @NonNull
    public static Intent newIntent(@NonNull Context context,
            ChangeMasterPasswordOrigin origin) {
        return newIntent(context, origin, false);
    }

    @NonNull
    public static Intent newIntent(@NonNull Context context,
            ChangeMasterPasswordOrigin origin,
            boolean warningDesktopShown) {
        return new Intent(context, ChangeMasterPasswordActivity.class)
                .putExtra(EXTRA_ORIGIN, origin)
                .putExtra(EXTRA_WARNING_DESKTOP_SHOWN, warningDesktopShown);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivity(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!presenter.onBackPressed()) {
            super.onBackPressed();
        }
    }

    private void setupActivity(@Nullable Bundle savedInstanceState) {
        SingletonProvider.getComponent().inject(this);

        ChangeMasterPasswordOrigin origin = getIntent().getParcelableExtra(EXTRA_ORIGIN);
        boolean warningDesktopShown = getIntent().getBooleanExtra(EXTRA_WARNING_DESKTOP_SHOWN, false);

        
        if (origin == null || !mChangeMasterPasswordFeatureAccessChecker
                .canAccessFeature(origin instanceof ChangeMasterPasswordOrigin.Migration)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_change_master_password);

        AnyPage page = getPage(origin);
        if (page != null) {
            PageViewUtil.setCurrentPageView(this, page);
        }

        ChangeMasterPasswordViewProxy viewProxy = new ChangeMasterPasswordViewProxy(this);
        ChangeMasterPasswordLogger logger =
                new ChangeMasterPasswordLogger(mSessionManager, mBySessionUsageLogRepository, origin.getSender(),
                        mLogRepository);
        presenter = new ChangeMasterPasswordPresenter(mPasswordStrengthEvaluator,
                logger,
                origin,
                warningDesktopShown,
                mChangeMasterPasswordLogoutHelper,
                LifecycleOwnerKt.getLifecycleScope(this));
        presenter.setProvider(mDataProvider);
        presenter.setView(viewProxy);
        presenter.onCreate(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        presenter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Nullable
    private static AnyPage getPage(ChangeMasterPasswordOrigin origin) {
        if (origin instanceof ChangeMasterPasswordOrigin.Settings) {
            return AnyPage.SETTINGS_SECURITY_CHANGE_MASTER_PASSWORD;
        } else {
            return null;
        }
    }
}
