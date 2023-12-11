package com.dashlane.login.dagger

import com.dashlane.login.pages.email.LoginEmailContract
import com.dashlane.login.pages.email.LoginEmailDataProvider
import com.dashlane.login.pages.email.LoginEmailLogger
import com.dashlane.login.pages.email.LoginEmailLoggerImpl
import com.dashlane.login.pages.password.LoginPasswordContract
import com.dashlane.login.pages.password.LoginPasswordDataProvider
import com.dashlane.login.pages.password.LoginPasswordLogger
import com.dashlane.login.pages.password.LoginPasswordLoggerImpl
import com.dashlane.login.pages.pin.PinLockContract
import com.dashlane.login.pages.pin.PinLockDataProvider
import com.dashlane.login.pages.sso.SsoLockContract
import com.dashlane.login.pages.sso.SsoLockDataProvider
import com.dashlane.login.pages.token.LoginTokenContract
import com.dashlane.login.pages.token.LoginTokenDataProvider
import com.dashlane.login.pages.totp.LoginTotpLogger
import com.dashlane.login.pages.totp.LoginTotpLoggerImpl
import com.dashlane.login.pages.totp.u2f.NfcServiceDetector
import com.dashlane.login.pages.totp.u2f.NfcServiceDetectorImpl
import com.dashlane.login.pages.totp.u2f.U2fKeyDetector
import com.dashlane.login.pages.totp.u2f.U2fKeyDetectorImpl
import com.dashlane.login.pages.totp.u2f.UsbServiceDetector
import com.dashlane.login.pages.totp.u2f.UsbServiceDetectorImpl
import com.dashlane.login.root.LoginContract
import com.dashlane.login.root.LoginDataProvider
import dagger.Binds
import dagger.Module

@Module(includes = [AuthBindingModule::class])
interface LoginBindingModule {
    @Binds
    fun bindDataProvider(loginDataProvider: LoginDataProvider): LoginContract.DataProvider

    @Binds
    fun bindEmailDataProvider(loginEmailDataProvider: LoginEmailDataProvider): LoginEmailContract.DataProvider

    @Binds
    fun bindTokenDataProvider(loginTokenDataProvider: LoginTokenDataProvider): LoginTokenContract.DataProvider

    @Binds
    fun bindPasswordDataProvider(loginPasswordDataProvider: LoginPasswordDataProvider): LoginPasswordContract.DataProvider

    @Binds
    fun bindEmailLogger(impl: LoginEmailLoggerImpl): LoginEmailLogger

    @Binds
    fun bindTotpLoggerFactory(loggerFactory: LoginTotpLoggerImpl.Factory): LoginTotpLogger.Factory

    @Binds
    fun bindPasswordLoggerFactory(loggerFactory: LoginPasswordLoggerImpl.Factory): LoginPasswordLogger.Factory

    @Binds
    fun bindNfcServiceDetector(nfcServiceDetector: NfcServiceDetectorImpl): NfcServiceDetector

    @Binds
    fun bindUsbServiceDetector(usbServiceDetector: UsbServiceDetectorImpl): UsbServiceDetector

    @Binds
    fun bindU2fKeyDetector(u2fKeyDetector: U2fKeyDetectorImpl): U2fKeyDetector

    @Binds
    fun bindBiometricDataProvider(biometricDataProvider: PinLockDataProvider): PinLockContract.DataProvider

    @Binds
    fun bindSsoLockDataProvider(impl: SsoLockDataProvider): SsoLockContract.DataProvider
}