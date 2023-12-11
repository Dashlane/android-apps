package com.dashlane.credentialmanager

import com.dashlane.credentialmanager.credential.CredentialPasskeyManager
import com.dashlane.credentialmanager.credential.CredentialPasskeyManagerImpl
import com.dashlane.credentialmanager.credential.CredentialPasswordManager
import com.dashlane.credentialmanager.credential.CredentialPasswordManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CredentialManagerModule {
    @Binds
    abstract fun bindCredentialLoader(credentialLoader: CredentialLoaderImpl): CredentialLoader

    @Binds
    abstract fun bindCredentialManagerHandler(credentialManagerHandler: CredentialManagerHandlerImpl): CredentialManagerHandler

    @Binds
    abstract fun bindCredentialPasskeyManager(passkeyManager: CredentialPasskeyManagerImpl): CredentialPasskeyManager

    @Binds
    abstract fun bindCredentialPasswordManager(credentialPasswordManager: CredentialPasswordManagerImpl): CredentialPasswordManager
}