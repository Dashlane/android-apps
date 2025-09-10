package com.dashlane.wear.hilt

import android.content.Context
import com.dashlane.credentialmanager.CredentialManagerDAO
import com.dashlane.credentialmanager.CredentialManagerDAOImpl
import com.dashlane.credentialmanager.CredentialManagerIntent
import com.dashlane.credentialmanager.authentication.CredentialBiometricAuthProvider
import com.dashlane.credentialmanager.model.CredentialManagerPlatform
import com.dashlane.ext.application.WhitelistApplication
import com.dashlane.ext.application.WhitelistApplicationImpl
import com.dashlane.vault.util.AuthentifiantPackageNameMatcher
import com.dashlane.vault.util.AuthentifiantPackageNameMatcherImpl
import com.dashlane.wear.credentialmanager.WearBiometricAuthProvider
import com.dashlane.wear.credentialmanager.WearCredentialManagerIntent
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface CredentialManagerModule {
    @Binds
    fun bindCredentialManagerIntent(impl: WearCredentialManagerIntent): CredentialManagerIntent

    @Binds
    fun bindCredentialManagerDatabase(impl: CredentialManagerDAOImpl): CredentialManagerDAO

    @Binds
    fun bindBiometricAuthProvider(impl: WearBiometricAuthProvider): CredentialBiometricAuthProvider

    @Binds
    fun bindAuthentifiantPackageNameMatcher(impl: AuthentifiantPackageNameMatcherImpl): AuthentifiantPackageNameMatcher

    companion object {
        @Provides
        fun provideWhiteListApplication(
            @ApplicationContext context: Context,
        ): WhitelistApplication = WhitelistApplicationImpl(context)

        @Provides
        fun provideCredentialManagerWearOsPlatform(): CredentialManagerPlatform = CredentialManagerPlatform.WEAR_OS
    }
}