package com.dashlane.createaccount

import com.dashlane.authentication.DeviceRegistrationInfo
import com.dashlane.authentication.RemoteKeyFactory
import com.dashlane.authentication.SettingsFactory
import com.dashlane.authentication.SettingsFactoryImpl
import com.dashlane.authentication.SsoServerKeyFactory
import com.dashlane.authentication.UuidFactory
import com.dashlane.authentication.create.AccountCreationEmailRepository
import com.dashlane.authentication.create.AccountCreationEmailRepositoryImpl
import com.dashlane.authentication.create.AccountCreationRepository
import com.dashlane.authentication.create.AccountCreationRepositoryImpl
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.SaltGenerator
import com.dashlane.cryptography.SharingCryptography
import com.dashlane.server.api.DashlaneTime
import com.dashlane.server.api.endpoints.account.AccountExistsService
import com.dashlane.server.api.endpoints.account.CreateAccountService
import com.dashlane.server.api.endpoints.account.CreateAccountSsoService
import com.dashlane.server.api.endpoints.country.GetIpCountryService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CreateAccountAuthModule {
    @Provides
    fun provideEmailRepository(accountExistsService: AccountExistsService): AccountCreationEmailRepository =
        AccountCreationEmailRepositoryImpl(accountExistsService)

    @Provides
    fun provideCreationRepository(
        createAccountService: CreateAccountService,
        dashlaneTime: DashlaneTime,
        deviceRegistrationinfo: DeviceRegistrationInfo,
        settingsFactory: SettingsFactory,
        uuidFactory: UuidFactory,
        createAccountSsoService: CreateAccountSsoService,
        cryptography: Cryptography,
        sharingCryptography: SharingCryptography,
        remoteKeyFactory: RemoteKeyFactory,
        ssoServerKeyFactory: SsoServerKeyFactory,
        getIpCountryService: GetIpCountryService,
    ): AccountCreationRepository = AccountCreationRepositoryImpl(
        createAccountService,
        dashlaneTime,
        deviceRegistrationinfo,
        settingsFactory,
        uuidFactory,
        createAccountSsoService,
        cryptography,
        sharingCryptography,
        remoteKeyFactory,
        ssoServerKeyFactory,
        getIpCountryService
    )

    @Provides
    fun provideSettingsFactory(saltGenerator: SaltGenerator): SettingsFactory =
        SettingsFactoryImpl(saltGenerator)
}