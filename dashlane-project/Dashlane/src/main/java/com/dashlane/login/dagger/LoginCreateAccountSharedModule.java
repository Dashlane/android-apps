package com.dashlane.login.dagger;

import android.app.Activity;

import androidx.fragment.app.FragmentActivity;

import com.dashlane.createaccount.CreateAccountActivity;
import com.dashlane.login.LoginActivity;
import com.dashlane.login.LoginSsoLoggerConfigProvider;
import com.dashlane.login.TrackingIdProvider;
import com.dashlane.login.settings.LoginSettingsActivity;
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactory;
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactoryImpl;
import com.dashlane.login.sso.LoginSsoLoggerModule;
import com.dashlane.useractivity.log.install.InstallLogCode69;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.Reusable;
import dagger.hilt.android.scopes.ActivityScoped;



@Module(includes = LoginSsoLoggerModule.class)
public abstract class LoginCreateAccountSharedModule {
    @Binds
    public abstract ContactSsoAdministratorDialogFactory bindLoginSsoContactAdministratorDialogFactory(
            ContactSsoAdministratorDialogFactoryImpl impl);

    @Reusable
    @Provides
    public static LoginSsoLoggerConfigProvider provideLoggingActivity(Activity activity) {
        return ((LoginSsoLoggerConfigProvider) activity);
    }

    @Provides
    @ActivityScoped
    @TrackingId
    static String getTrackingId(FragmentActivity fragmentActivity) {
        return TrackingIdProvider.INSTANCE.getOrGenerateTrackingId(fragmentActivity);
    }

    @Provides
    @Reusable
    static InstallLogCode69.Type getUsageLogType(Activity activity) {
        if (activity instanceof LoginActivity || activity instanceof LoginSettingsActivity) {
            return InstallLogCode69.Type.LOGIN;
        } else if (activity instanceof CreateAccountActivity) {
            return InstallLogCode69.Type.CREATE_ACCOUNT;
        } else {
            throw new IllegalStateException("Unexpected login activity " + activity.getClass().getCanonicalName());
        }
    }
}
