package com.dashlane.login.sso.migration;

import com.dashlane.login.dagger.AuthBindingModule;
import com.dashlane.login.dagger.LoginAuthModule;
import com.dashlane.login.sso.LoginSsoLoggerModule;
import com.dashlane.server.api.dagger.DashlaneApiEndpointsModule;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityComponent;

@Module(includes = {LoginAuthModule.class, AuthBindingModule.class, DashlaneApiEndpointsModule.class,
        LoginSsoLoggerModule.class})
@InstallIn(ActivityComponent.class)
public abstract class MigrationToSsoMemberModule {
    @Binds
    abstract MigrationToSsoMemberContract.DataProvider bindDataProvider(MigrationToSsoMemberDataProvider impl);

    @Provides
    static MigrationToSsoMemberContract.Presenter providePresenter(
            MigrationToSsoMemberPresenter impl,
            MigrationToSsoMemberContract.DataProvider dataProvider
    ) {
        impl.setProvider(dataProvider);
        return impl;
    }
}
