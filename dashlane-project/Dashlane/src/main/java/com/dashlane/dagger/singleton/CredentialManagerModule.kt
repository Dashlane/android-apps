package com.dashlane.dagger.singleton

import com.dashlane.credentialmanager.CredentialManagerDAO
import com.dashlane.credentialmanager.CredentialManagerDAOImpl
import com.dashlane.credentialmanager.CredentialManagerIntent
import com.dashlane.credentialmanager.CredentialManagerIntentImpl
import com.dashlane.credentialmanager.MobileBiometricAuthProvider
import com.dashlane.credentialmanager.authentication.CredentialBiometricAuthProvider
import com.dashlane.credentialmanager.model.CredentialManagerPlatform
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface CredentialManagerModule {
    @Binds
    fun bindCredentialManagerIntent(impl: CredentialManagerIntentImpl): CredentialManagerIntent

    @Binds
    fun bindCredentialManagerDatabase(impl: CredentialManagerDAOImpl): CredentialManagerDAO

    @Binds
    fun bindBiometricAuthProvider(impl: MobileBiometricAuthProvider): CredentialBiometricAuthProvider

    companion object {
        @Provides
        fun provideCredentialManagerMobilePlatform(): CredentialManagerPlatform = CredentialManagerPlatform.ANDROID
    }
}