package com.dashlane.login.root

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.dashlane.account.UserAccountInfo
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.lock.LockSetting
import com.dashlane.login.lock.LockTypeManager
import com.dashlane.login.pages.LoginBaseContract
import com.dashlane.login.pages.authenticator.LoginDashlaneAuthenticatorContract
import com.dashlane.login.pages.biometric.BiometricContract
import com.dashlane.login.pages.email.LoginEmailContract
import com.dashlane.login.pages.password.LoginPasswordContract
import com.dashlane.login.pages.pin.PinLockContract
import com.dashlane.login.pages.sso.SsoLockContract
import com.dashlane.login.pages.token.LoginTokenContract
import com.dashlane.login.pages.totp.LoginTotpContract
import com.dashlane.login.sso.MigrationToSsoMemberInfo
import com.skocken.presentation.definition.Base

interface LoginContract {

    interface LoginViewProxy : Base.IView {

        fun transitionTo(presenter: LoginBaseContract.Presenter)

        fun transition(from: LoginBaseContract.Presenter?, to: LoginBaseContract.Presenter)

        fun transitionToCompose(email: String?, startDestination: String)
    }

    interface Presenter : Base.IPresenter {
        fun onCreate(savedInstanceState: Bundle?)

        fun onSaveInstanceState(outState: Bundle)

        fun onBackPressed(): Boolean

        var showProgress: Boolean

        fun onDestroy()

        fun onStart()

        fun onPrimaryFactorCancelOrLogout()

        fun onPrimaryFactorTooManyAttempts()

        fun onBiometricNegativeClicked()
    }

    interface DataProvider : Base.IDataProvider {

        val layoutInflater: LayoutInflater

        val currentUserInfo: UserAccountInfo?

        val lockSetting: LockSetting

        @LockTypeManager.LockType
        fun getLockType(context: Context): Int

        fun createEmailDataProvider(): LoginEmailContract.DataProvider

        fun createTokenDataProvider(emailSecondFactor: AuthenticationSecondFactor.EmailToken): LoginTokenContract.DataProvider

        fun createAuthenticatorProvider(secondFactor: AuthenticationSecondFactor): LoginDashlaneAuthenticatorContract.DataProvider

        fun createTotpDataProvider(totpSecondFactor: AuthenticationSecondFactor.Totp): LoginTotpContract.DataProvider

        fun createPasswordDataProvider(
            registeredUserDevice: RegisteredUserDevice,
            authTicket: String?,
            migrationToSsoMemberInfo: MigrationToSsoMemberInfo?,
            topicLock: String?,
            allowBypass: Boolean
        ): LoginPasswordContract.DataProvider

        fun createBiometricDataProvider(): BiometricContract.DataProvider

        fun createPinLockDataProvider(): PinLockContract.DataProvider

        fun createSsoLockDataProvider(): SsoLockContract.DataProvider

        suspend fun initializeStoredSession(login: String, serverKey: String?)

        fun isAlreadyLoggedIn(): Boolean

        fun forceMasterPasswordUnlock(unlockReason: UnlockEvent.Reason?): Boolean

        fun canDelayMasterPasswordUnlock(): Boolean
    }
}
