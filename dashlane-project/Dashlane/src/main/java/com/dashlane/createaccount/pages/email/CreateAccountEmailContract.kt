package com.dashlane.createaccount.pages.email

import android.content.Intent
import androidx.annotation.StringRes
import com.dashlane.createaccount.pages.CreateAccountBaseContract
import com.skocken.presentation.definition.Base

interface CreateAccountEmailContract {

    interface ViewProxy : Base.IView {

        var emailText: String

        fun showConfirmEmailPopup(
            email: String,
            inEuropeanUnion: Boolean,
            country: String?,
            loginSsoIntent: Intent?,
            callback: () -> Unit = {}
        )

        fun showError(@StringRes errorResId: Int)

        fun exposeTrackingInstallationId(id: String?)
    }

    interface Presenter : CreateAccountBaseContract.Presenter {

        fun onConfirmEmail(
            email: String,
            inEuropeanUnion: Boolean,
            country: String?,
            loginSsoIntent: Intent?
        )
    }

    interface DataProvider : CreateAccountBaseContract.DataProvider {
        suspend fun validateEmail(email: String): PendingAccount

        fun getTrackingInstallationId(): String
    }

    data class PendingAccount(
        val email: String,
        val emailLikelyInvalid: Boolean,
        val inEuropeanUnion: Boolean,
        val country: String?,
        val loginSsoIntent: Intent?
    )

    class EmptyEmailException(cause: Throwable? = null) : Exception(cause)
    class InvalidEmailException(cause: Throwable? = null) : Exception(cause)
    class AccountAlreadyExistsException(cause: Throwable? = null) : Exception(cause)
    class ContactSsoAdministratorException(cause: Throwable? = null) : Exception(cause)
    class ExpiredVersionException(cause: Throwable? = null) : Exception(cause)
}