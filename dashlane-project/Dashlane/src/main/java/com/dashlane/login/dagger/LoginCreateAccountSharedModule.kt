package com.dashlane.login.dagger

import androidx.fragment.app.FragmentActivity
import com.dashlane.login.TrackingIdProvider
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactory
import com.dashlane.login.sso.ContactSsoAdministratorDialogFactoryImpl
import com.dashlane.login.sso.LoginSsoLoggerModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module(includes = [LoginSsoLoggerModule::class])
@InstallIn(ActivityComponent::class)
abstract class LoginCreateAccountSharedModule {

    @Binds
    abstract fun bindContactSsoAdministratorDialogFactory(factory: ContactSsoAdministratorDialogFactoryImpl): ContactSsoAdministratorDialogFactory

    companion object {
        @Provides
        @TrackingId
        fun getTrackingId(fragmentActivity: FragmentActivity): String = TrackingIdProvider.getOrGenerateTrackingId(fragmentActivity)
    }
}