package com.dashlane.createaccount

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.dashlane.user.UserAccountInfo
import com.dashlane.authentication.create.AccountCreator
import com.dashlane.createaccount.pages.choosepassword.CreateAccountChoosePasswordContract
import com.dashlane.createaccount.pages.confirmpassword.CreateAccountConfirmPasswordContract
import com.dashlane.createaccount.pages.email.CreateAccountEmailContract
import com.dashlane.cryptography.ObfuscatedByteArray
import com.skocken.presentation.definition.Base

interface CreateAccountContract {

    interface Presenter : Base.IPresenter {
        fun onCreate(savedInstanceState: Bundle?)

        fun onSaveInstanceState(outState: Bundle)

        fun onBackPressed(): Boolean

        fun onNextClicked()

        fun onMplessSetupClicked()

        var showProgress: Boolean

        fun onDestroy()
        fun onStart()
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
        fun updateSettings(biometric: Boolean, resetMp: Boolean)
    }

    interface ViewProxy : Base.IView {
        val root: ConstraintLayout

        val content: ViewGroup

        var showProgress: Boolean

        var nextEnabled: Boolean

        var mplessButtonVisible: Boolean

        fun navigateNext(completion: (() -> Unit)? = null)

        fun navigatePrevious()

        fun navigateLast()
        fun showError(@StringRes errorResId: Int)
    }

    interface DataProvider : Base.IDataProvider {

        val layoutInflater: LayoutInflater

        suspend fun createAccount(
            username: String,
            masterPassword: ObfuscatedByteArray,
            accountType: UserAccountInfo.AccountType,
            termsState: AccountCreator.TermsState,
            biometricEnabled: Boolean,
            resetMpEnabled: Boolean,
            country: String
        )

        fun createEmailDataProvider(): CreateAccountEmailContract.DataProvider

        fun createChoosePasswordDataProvider(username: String, isB2B: Boolean): CreateAccountChoosePasswordContract.DataProvider

        fun createConfirmPasswordDataProvider(
            username: String,
            password: ObfuscatedByteArray,
            inEuropeanUnion: Boolean,
            origin: String?,
            country: String
        ): CreateAccountConfirmPasswordContract.DataProvider

        fun createSuccessIntent(): Intent

        fun isExplicitOptinRequired(inEuropeanUnion: Boolean): Boolean
    }
}
