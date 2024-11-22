package com.dashlane.login.sso.migration

import com.dashlane.login.dagger.AuthBindingModule
import com.dashlane.authentication.hilt.LoginAuthModule
import com.dashlane.login.sso.LoginSsoLoggerModule
import com.dashlane.server.api.dagger.DashlaneApiEndpointsModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module(
    includes = [
        LoginAuthModule::class,
        AuthBindingModule::class,
        DashlaneApiEndpointsModule::class,
        LoginSsoLoggerModule::class
    ],
)
@InstallIn(ActivityComponent::class)
abstract class MigrationToSsoMemberModule {

    @Binds
    abstract fun bindDataProvider(dataProvider: MigrationToSsoMemberDataProvider): MigrationToSsoMemberContract.DataProvider

    companion object {
        @Provides
        fun providePresenter(
            presenter: MigrationToSsoMemberPresenter,
            dataProvider: MigrationToSsoMemberContract.DataProvider
        ): MigrationToSsoMemberContract.Presenter {
            presenter.setProvider(dataProvider)
            return presenter
        }
    }
}