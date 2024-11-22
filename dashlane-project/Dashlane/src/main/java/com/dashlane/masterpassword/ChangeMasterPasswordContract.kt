package com.dashlane.masterpassword

import android.os.Bundle
import android.text.Editable
import com.dashlane.changemasterpassword.ChangeMasterPasswordOrigin
import com.dashlane.changemasterpassword.MasterPasswordChanger
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.passwordstrength.PasswordStrength
import com.skocken.presentation.definition.Base
import kotlinx.coroutines.flow.StateFlow

interface ChangeMasterPasswordContract {

    interface DataProvider : Base.IDataProvider {

        val progressStateFlow: StateFlow<MasterPasswordChanger.Progress>

        suspend fun clearChannel()

        suspend fun updateMasterPassword(newPassword: ObfuscatedByteArray, origin: ChangeMasterPasswordOrigin)

        suspend fun migrateToMasterPasswordUser(password: ObfuscatedByteArray, authTicket: String)
    }

    interface Presenter : Base.IPresenter {
        fun onCreate(savedInstanceState: Bundle?)
        fun onNextClicked(password: CharSequence)
        fun onTipsClicked()
        fun onBackPressed(): Boolean
        fun onSaveInstanceState(outState: Bundle)
    }

    interface View : Base.IView {
        fun setOnPasswordChangeListener(onPasswordChange: (Editable) -> Unit)
        fun configureStrengthLevel(message: String, passwordStrength: PasswordStrength)
        fun clearPassword()
        fun setPassword(password: String)
        fun setTitle(title: String)
        fun setNextButtonText(text: String)
        fun showTipsButton(visible: Boolean)
        fun showStrengthLevel(visible: Boolean)
        fun showError(error: String)
        fun showLoader()
        fun hideLoader()
        fun setProgress(progress: Float)
        suspend fun displaySuccess(animate: Boolean)
    }
}