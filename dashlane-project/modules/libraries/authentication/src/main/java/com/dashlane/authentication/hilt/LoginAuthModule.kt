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
import com.dashlane.server.api.endpoints.authentication.CompleteDeviceRegistrationWithAuthTicketService
import com.dashlane.server.api.endpoints.authentication.CompleteExtraDeviceRegistrationWithTokenService
import com.dashlane.server.api.endpoints.authentication.CompleteLoginWithAuthTicketService
import com.dashlane.server.api.endpoints.authentication.Get2FAStatusUnauthenticatedService
import com.dashlane.server.api.endpoints.authentication.GetAuthenticationMethodsForDeviceService
import com.dashlane.server.api.endpoints.authentication.GetAuthenticationMethodsForLoginService
import com.dashlane.server.api.endpoints.authentication.PerformDuoPushVerificationService
import com.dashlane.server.api.endpoints.authentication.PerformEmailTokenVerificationService
import com.dashlane.server.api.endpoints.authentication.PerformSsoVerificationService
import com.dashlane.server.api.endpoints.authentication.PerformTotpVerificationService
import com.dashlane.server.api.endpoints.authentication.PerformU2FVerificationService
import com.dashlane.server.api.endpoints.authentication.RequestEmailTokenVerificationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object LoginAuthModule {
    @Provides
    fun provideEmailRepository(
        getAuthenticationMethodsForDeviceService: GetAuthenticationMethodsForDeviceService,
        userStorage: UserStorage,
        getAuthenticationMethodsForLoginService: GetAuthenticationMethodsForLoginService,
        requestEmailTokenVerificationService: RequestEmailTokenVerificationService,
        get2FAStatusUnauthenticatedService: Get2FAStatusUnauthenticatedService,
        connectivityCheck: ConnectivityCheck,
        dashlaneTime: DashlaneTime,
    ): AuthenticationEmailRepository = AuthenticationEmailRepositoryImpl(
        userStorage,
        getAuthenticationMethodsForDeviceService,
        getAuthenticationMethodsForLoginService,
        requestEmailTokenVerificationService,
        get2FAStatusUnauthenticatedService,
        connectivityCheck,
        dashlaneTime,
    )

    @Provides
    fun provideSecondFactorRepository(
        requestEmailTokenVerificationService: RequestEmailTokenVerificationService,
        connectivityCheck: ConnectivityCheck,
        userStorage: UserStorage,
        authTicketHelper: AuthenticationAuthTicketHelper,
    ): AuthenticationSecondFactoryRepository = AuthenticationSecondFactoryRepositoryImpl(
        userStorage,
        requestEmailTokenVerificationService,
        connectivityCheck,
        authTicketHelper,
    )

    @Provides
    fun providePasswordRepository(
        userStorage: UserStorage,
        authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
        getAuthenticationMethodsForLoginService: GetAuthenticationMethodsForLoginService,
        completeExtraDeviceRegistrationWithTokenService: CompleteExtraDeviceRegistrationWithTokenService,
        deviceRegistrationInfo: DeviceRegistrationInfo,
        cryptography: Cryptography,
    ): AuthenticationPasswordRepository = AuthenticationPasswordRepositoryImpl(
        userStorage,
        authenticationLocalKeyRepository,
        getAuthenticationMethodsForLoginService,
        completeExtraDeviceRegistrationWithTokenService,
        deviceRegistrationInfo,
        cryptography,
    )

    @Provides
    fun provideSsoRepository(
        connectivityCheck: ConnectivityCheck,
        userStorage: UserStorage,
        getAuthenticationMethodsForLoginService: GetAuthenticationMethodsForLoginService,
        authTicketHelper: AuthenticationAuthTicketHelper,
        authenticationLocalKeyRepository: AuthenticationLocalKeyRepository,
        cryptography: Cryptography,
    ): AuthenticationSsoRepository = AuthenticationSsoRepositoryImpl(
        connectivityCheck,
        userStorage,
        getAuthenticationMethodsForLoginService,
        authTicketHelper,
        authenticationLocalKeyRepository,
        cryptography,
    )

    @Provides
    fun provideAuthTicketHelper(
        deviceRegistrationInfo: DeviceRegistrationInfo,
        performEmailTokenVerificationService: PerformEmailTokenVerificationService,
        performTotpVerificationService: PerformTotpVerificationService,
        performU2FVerificationService: PerformU2FVerificationService,
        performDuoPushVerificationService: PerformDuoPushVerificationService,
        performSsoVerificationService: PerformSsoVerificationService,
        completeDeviceRegistrationWithAuthTicketService: CompleteDeviceRegistrationWithAuthTicketService,
        completeLoginWithAuthTicketService: CompleteLoginWithAuthTicketService,
    ): AuthenticationAuthTicketHelper = AuthenticationAuthTicketHelperImpl(
        deviceRegistrationInfo,
        performEmailTokenVerificationService,
        performTotpVerificationService,
        performU2FVerificationService,
        performDuoPushVerificationService,
        performSsoVerificationService,
        completeDeviceRegistrationWithAuthTicketService,
        completeLoginWithAuthTicketService,
    )
}