package com.dashlane.createaccount;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.dashlane.R;
import com.dashlane.cryptography.ObfuscatedByteArray;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.login.dagger.TrackingId;
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactory;
import com.dashlane.ui.AutoFillDisablerKt;
import com.dashlane.ui.ScreeenshotEnablerKt;
import com.dashlane.ui.activities.DashlaneActivity;
import com.dashlane.ui.endoflife.EndOfLife;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwnerKt;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreateAccountActivity extends DashlaneActivity {

    public static final String EXTRA_PRE_FILLED_EMAIL = "pre_filled_email";
    public static final String EXTRA_SKIP_EMAIL_IF_PRE_FILLED = "skipEmailIfPrefilled";

    @Inject
    CreateAccountContract.DataProvider mDataProvider;

    @Inject
    ContactSsoAdministratorDialogFactory mContactSsoAdministratorDialogFactory;

    @Inject
    @TrackingId
    String trackingId;

    @Inject
    EndOfLife mEndOfLife;

    CreateAccountPresenter mPresenter;

    @Override
    public boolean getRequireUserUnlock() {
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AutoFillDisablerKt.disableAutoFill(this);
        setContentView(R.layout.activity_login_create_account);
        ScreeenshotEnablerKt.applyScreenshotAllowedFlag(getWindow(), SingletonProvider.getScreenshotPolicy());

        View view = findViewById(R.id.view_login_root);
        CreateAccountViewProxy viewProxy = new CreateAccountViewProxy(view);
        mPresenter = new CreateAccountPresenter(LifecycleOwnerKt.getLifecycleScope(this),
                                                getIntent().getStringExtra(EXTRA_PRE_FILLED_EMAIL),
                                                getIntent().getBooleanExtra(EXTRA_SKIP_EMAIL_IF_PRE_FILLED, false),
                                                mEndOfLife,
                                                mContactSsoAdministratorDialogFactory);
        mPresenter.setView(viewProxy);
        mPresenter.setProvider(mDataProvider);
        ObfuscatedByteArray password = (ObfuscatedByteArray) getLastCustomNonConfigurationInstance();
        mPresenter.setPassword(password);
        mPresenter.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.onStart();
    }

    @Override
    public void onBackPressed() {
        if (!mPresenter.onBackPressed()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return mPresenter.getPassword();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mPresenter.onSaveInstanceState(outState);
    }

    @SuppressWarnings("DEPRECATION")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPresenter.onActivityResult(requestCode, resultCode, data);
    }
}
