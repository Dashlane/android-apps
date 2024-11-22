package com.dashlane.authentication.hilt

import com.dashlane.authentication.DeviceRegistrationInfo
import com.dashlane.authentication.UserStorage
import com.dashlane.authentication.localkey.AuthenticationLocalKeyRepository
import com.dashlane.authentication.login.AuthenticationAuthTicketHelper
import com.dashlane.authentication.login.AuthenticationAuthTicketHelperImpl
import com.dashlane.authentication.login.AuthenticationEmailRepository
import com.dashlane.authentication.login.AuthenticationEmailRepositoryImpl
import com.dashlane.authentication.login.AuthenticationPasswordRepository
import com.dashlane.authentication.login.AuthenticationPasswordRepositoryImpl
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepository
import com.dashlane.authentication.login.AuthenticationSecondFactoryRepositoryImpl
import com.dashlane.authentication.login.AuthenticationSsoRepository
import com.dashlane.authentication.login.AuthenticationSsoRepositoryImpl
import com.dashlane.cryptography.Cryptography
import com.dashlane.server.api.ConnectivityCheck
import com.dashlane.server.api.DashlaneTime
import com.dashlane.server.api.endpoints.authentication.Auth2faUnauthenticatedSettingsService
import com.dashlane.server.api.endpoints.authentication.AuthLoginAuthTicketService
import com.dashlane.server.api.endpoints.authentication.AuthLoginService
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationAuthTicketService
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationEmailService
import com.dashlane.server.api.endpoints.authentication.AuthRegistrationExtraDeviceService
import com.dashlane.server.api.endpoints.authentication.AuthSendEmailTokenService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationDuoPushService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationEmailTokenService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationSsoService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationTotpService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationU2fService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object LoginAuthModule {
    @Provides
    fun provideEmailRepository(
        authRegistrationEmailService: AuthRegistrationEmailService,
        userStorage: UserStorage,
        authLoginService: AuthLoginService,
        authSendEmailTokenService: AuthSendEmailTokenService,
        unauthenticated2faSettingsService: Auth2faUnauthenticatedSettingsService,
        connectivityCheck: ConnectivityCheck,
        dashlaneTime: DashlaneTime
    ): AuthenticationEmailRepository = AuthenticationEmailRepositoryImpl(
        userStorage,
        authRegistrationEmailService,
        authLoginService,
        authSendEmailTokenService,
        unauthenticated2faSettingsService,
        connectivityCheck,
        dashlaneTime
    )

    @Provides
    fun provideSecondFactorRepository(
        sendEmailTokenService: AuthSendEmailTokenService,
        connectivityCheck: ConnectivityCheck,
        userStorage: UserStorage,
        authTicketHelper: AuthenticationAuthTicketHelper
    ): AuthenticationSecondFactoryRepository = AuthenticationSecondFactoryRepositoryImpl(
        userStorage,
        sendEmailTokenService,
        connectivityCheck,
        authTicketHelper
    )

    @Provides
    fun providePasswordRepository(
        userStorage: UserStorage,
        authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
        loginService: AuthLoginService,
        authRegistrationExtraDeviceService: AuthRegistrationExtraDeviceService,
        deviceRegistrationInfo: DeviceRegistrationInfo,
        cryptography: Cryptography
    ): AuthenticationPasswordRepository = AuthenticationPasswordRepositoryImpl(
        userStorage,
        authenticationLocalKeyRepository,
        loginService,
        authRegistrationExtraDeviceService,
        deviceRegistrationInfo,
        cryptography
    )

    @Provides
    fun provideSsoRepository(
        connectivityCheck: ConnectivityCheck,
        userStorage: UserStorage,
        loginService: AuthLoginService,
        authTicketHelper: AuthenticationAuthTicketHelper,
        authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
        cryptography: Cryptography
    ): AuthenticationSsoRepository = AuthenticationSsoRepositoryImpl(
        connectivityCheck,
        userStorage,
        loginService,
        authTicketHelper,
        authenticationLocalKeyRepository,
        cryptography
    )

    @Provides
    fun provideAuthTicketHelper(
        deviceRegistrationInfo: DeviceRegistrationInfo,
        verificationEmailTokenService: AuthVerificationEmailTokenService,
        verificationTotpService: AuthVerificationTotpService,
        verificationU2fService: AuthVerificationU2fService,
        verificationDuoPushService: AuthVerificationDuoPushService,
        verificationSsoService: AuthVerificationSsoService,
        registrationAuthTicketService: AuthRegistrationAuthTicketService,
        loginAuthTicketService: AuthLoginAuthTicketService
    ): AuthenticationAuthTicketHelper = AuthenticationAuthTicketHelperImpl(
        deviceRegistrationInfo,
        verificationEmailTokenService,
        verificationTotpService,
        verificationU2fService,
        verificationDuoPushService,
        verificationSsoService,
        registrationAuthTicketService,
        loginAuthTicketService
    )
}
