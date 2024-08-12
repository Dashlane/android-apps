package com.dashlane.login.sso

import com.dashlane.createaccount.CreateAccountAuthModule
import com.dashlane.login.dagger.AuthBindingModule
import com.dashlane.login.dagger.LoginAuthModule
import com.dashlane.network.inject.DashlaneServicesModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module(
    includes = [
        AuthBindingModule::class,
        LoginAuthModule::class,
        CreateAccountAuthModule::class,
        LoginSsoLoggerModule::class,
        DashlaneServicesModule::class
    ]
)
@InstallIn(ActivityComponent::class)
abstract class LoginSsoModule {

    @Binds
    abstract fun bindDataProvider(provider: LoginSsoDataProvider): LoginSsoContract.DataProvider

    companion object {
        @Provides
        fun providePresenter(
            presenter: LoginSsoPresenter,
            provider: LoginSsoContract.DataProvider,
        ): LoginSsoContract.Presenter {
            presenter.setProvider(provider)
            return presenter
        }
    }
}