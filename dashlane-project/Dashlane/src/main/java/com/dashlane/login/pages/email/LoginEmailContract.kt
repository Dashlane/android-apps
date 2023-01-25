package com.dashlane.login.pages.email

import android.content.Context
import android.content.Intent
import com.dashlane.authentication.AuthenticationSecondFactor
import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.login.pages.LoginBaseContract
import com.dashlane.login.sso.MigrationToSsoMemberInfo
import kotlinx.coroutines.CoroutineScope



interface LoginEmailContract {

    interface ViewProxy : LoginBaseContract.View {

        

        var emailText: String

        

        fun setSuggestion(suggestions: List<String>?)

        

        fun exposeTrackingInstallationId(id: String?)
    }

    interface Presenter : LoginBaseContract.Presenter {

        var email: String?

        

        fun onAutoFill()

        

        fun onClear()

        

        fun notifyUnknownError()

        

        fun notifyEmailError()

        

        fun showTokenPage(secondFactor: AuthenticationSecondFactor.EmailToken)

        

        fun showAuthenticatorPage(secondFactor: AuthenticationSecondFactor.EmailToken)

        

        fun showOtpPage(secondFactor: AuthenticationSecondFactor.Totp)

        

        fun showPasswordStep(registeredUserDevice: RegisteredUserDevice)

        

        fun startLoginSso(intent: Intent)

        

        fun login(email: String, delay: Boolean = false)

        

        fun skipIfPrefilled()

        

        fun onCreateAccountClicked(skipEmailIfPrefilled: Boolean = false)

        

        fun setMigrationToSsoMember(migrationToSsoMemberInfo: MigrationToSsoMemberInfo)
    }

    interface DataProvider : LoginBaseContract.DataProvider {

        val loginHistory: List<String>?

        

        val preferredLogin: String?

        

        val preFillLogin: Boolean

        

        fun onAutoFill()

        

        fun onClear()

        

        suspend fun executeLogin(email: String)

        

        fun accountCreation()

        

        fun uploadUserSupportFile(context: Context, coroutineScope: CoroutineScope)

        

        fun getTrackingInstallationId(): String
    }

    class EmptyEmailException(cause: Throwable? = null) : Exception(cause)
    class InvalidEmailException(cause: Throwable? = null) : Exception(cause)
    class ContactSsoAdministratorException(cause: Throwable? = null) : Exception(cause)
}