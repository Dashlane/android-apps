package com.dashlane.login.dagger;

import com.dashlane.login.TrackingIdProvider;
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactory;
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactoryImpl;
import com.dashlane.login.sso.LoginSsoLoggerModule;

import androidx.fragment.app.FragmentActivity;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.android.scopes.ActivityScoped;

@Module(includes = LoginSsoLoggerModule.class)
public abstract class LoginCreateAccountSharedModule {
    @Binds
    public abstract ContactSsoAdministratorDialogFactory bindLoginSsoContactAdministratorDialogFactory(
            ContactSsoAdministratorDialogFactoryImpl impl);

    @Provides
    @ActivityScoped
    @TrackingId
    static String getTrackingId(FragmentActivity fragmentActivity) {
        return TrackingIdProvider.INSTANCE.getOrGenerateTrackingId(fragmentActivity);
    }
}
