package com.dashlane.createaccount

import com.dashlane.authentication.DeviceRegistrationInfo
import com.dashlane.authentication.RemoteKeyFactory
import com.dashlane.authentication.SettingsFactory
import com.dashlane.authentication.SettingsFactoryImpl
import com.dashlane.authentication.SsoServerKeyFactory
import com.dashlane.authentication.create.AccountCreationEmailRepository
import com.dashlane.authentication.create.AccountCreationEmailRepositoryImpl
import com.dashlane.authentication.create.AccountCreationRepository
import com.dashlane.authentication.create.AccountCreationRepositoryImpl
import com.dashlane.cryptography.Cryptography
import com.dashlane.cryptography.SaltGenerator
import com.dashlane.cryptography.SharingCryptography
import com.dashlane.server.api.DashlaneTime
import com.dashlane.server.api.endpoints.account.CreateUserService
import com.dashlane.server.api.endpoints.account.CreateUserWithSSOService
import com.dashlane.server.api.endpoints.account.RequestAccountCreationService
import com.dashlane.server.api.endpoints.country.GetIpCountryService
import com.dashlane.util.uuid.UuidFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CreateAccountAuthModule {
    @Provides
    fun provideEmailRepository(requestAccountCreationService: RequestAccountCreationService): AccountCreationEmailRepository =
        AccountCreationEmailRepositoryImpl(requestAccountCreationService)

    @Provides
    fun provideCreationRepository(
        createUserService: CreateUserService,
        dashlaneTime: DashlaneTime,
        deviceRegistrationinfo: DeviceRegistrationInfo,
        settingsFactory: SettingsFactory,
        uuidFactory: UuidFactory,
        createUserWithSSOService: CreateUserWithSSOService,
        cryptography: Cryptography,
        sharingCryptography: SharingCryptography,
        remoteKeyFactory: RemoteKeyFactory,
        ssoServerKeyFactory: SsoServerKeyFactory,
        getIpCountryService: GetIpCountryService,
    ): AccountCreationRepository = AccountCreationRepositoryImpl(
        createUserService,
        dashlaneTime,
        deviceRegistrationinfo,
        settingsFactory,
        uuidFactory,
        createUserWithSSOService,
        cryptography,
        sharingCryptography,
        remoteKeyFactory,
        ssoServerKeyFactory,
        getIpCountryService,
    )

    @Provides
    fun provideSettingsFactory(saltGenerator: SaltGenerator): SettingsFactory =
        SettingsFactoryImpl(saltGenerator)
}