package com.dashlane.login.pages.email

import android.content.Intent
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.login.pages.LoginBaseContract
import com.dashlane.login.sso.MigrationToSsoMemberInfo

interface LoginEmailContract {

    interface ViewProxy : LoginBaseContract.View {

        var emailText: String

        fun setSuggestion(suggestions: List<String>?)

        fun exposeTrackingInstallationId(id: String?)

        fun showConfirmationDialog()

        fun showUploadingDialog()

        fun showUploadFinishedDialog(crashDeviceId: String, copy: (String) -> Unit)

        fun showUploadFailedDialog()
    }

    interface Presenter : LoginBaseContract.Presenter {

        var email: String?

        fun onAutoFill()

        fun notifyUnknownError()

        fun notifyTeamError()

        fun notifyEmailError()

        fun showTokenPage(secondFactor: AuthenticationSecondFactor.EmailToken)

        fun showAuthenticatorPage(secondFactor: AuthenticationSecondFactor.EmailToken)

        fun showOtpPage(secondFactor: AuthenticationSecondFactor.Totp)

        fun showSecretTransferQRPage()

        fun showPasswordStep(registeredUserDevice: RegisteredUserDevice)

        fun startLoginSso(intent: Intent)

        fun login(email: String, delay: Boolean = false)

        fun skipIfPrefilled()

        fun onCreateAccountClicked(skipEmailIfPrefilled: Boolean = false)

        fun setMigrationToSsoMember(migrationToSsoMemberInfo: MigrationToSsoMemberInfo)

        fun askUserForConfirmation()

        fun userSupportFileConfirmed()

        fun showUploadingDialog()

        fun showUploadFinishedDialog(crashDeviceId: String, copy: (String) -> Unit)

        fun showUploadFailedDialog()
    }

    interface DataProvider : LoginBaseContract.DataProvider {

        val loginHistory: List<String>?

        val preferredLogin: String?

        val preFillLogin: Boolean

        fun onAutoFill()

        suspend fun executeLogin(email: String)

        fun uploadUserSupportFile()

        fun getTrackingInstallationId(): String
    }

    class EmptyEmailException(cause: Throwable? = null) : Exception(cause)
    class InvalidEmailException(cause: Throwable? = null) : Exception(cause)
    class ContactSsoAdministratorException(cause: Throwable? = null) : Exception(cause)
}
