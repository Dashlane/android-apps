package com.dashlane.login.sso;

import com.dashlane.account.UserAccountStorage;
import com.dashlane.createaccount.CreateAccountAuthModule;
import com.dashlane.login.AccountDataLossTrackingListener;
import com.dashlane.login.dagger.AuthBindingModule;
import com.dashlane.login.dagger.LoginAuthModule;
import com.dashlane.network.inject.DashlaneServicesModule;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;

@Module(includes = {
        AuthBindingModule.class,
        LoginAuthModule.class,
        CreateAccountAuthModule.class,
        LoginSsoLoggerModule.class,
        DashlaneServicesModule.class
})
@InstallIn(ActivityComponent.class)
abstract class LoginSsoModule {

    @Binds
    abstract LoginSsoContract.DataProvider bindDataProvider(LoginSsoDataProvider impl);

    @Binds
    abstract UserAccountStorage.DataLossTrackingListener bindDataLossTrackingListener(
            AccountDataLossTrackingListener impl);

    @Provides
    static LoginSsoContract.Presenter providePresenter(LoginSsoPresenter impl,
                                                       LoginSsoContract.DataProvider provider) {
        impl.setProvider(provider);
        return impl;
    }
}