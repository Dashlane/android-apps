package com.dashlane.ui.activities.firstpassword

import android.graphics.drawable.Drawable
import android.os.Bundle
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.definition.Base

interface AddFirstPassword {
    interface Presenter : Base.IPresenter {
        fun onCreate(url: String, savedInstanceState: Bundle?)
        fun onSaveInstanceState(outState: Bundle)
        fun onButtonSecureClicked()
        fun onButtonSaveClicked(login: String, password: String)
        fun onButtonTryDemoClicked()
        fun onButtonReturnHomeClicked()
        fun onDestroy()
    }

    interface ViewProxy : Base.IView {
        fun setLogin(email: String)
        fun setupToolbar(drawable: Drawable)
        fun displayAutofillDemoPrompt()
        fun dismissAutofillDemoPrompt()
    }

    interface DataProvider : Base.IDataProvider {
        fun createCredential(url: String, login: String, password: String): VaultItem<SyncObject.Authentifiant>
        suspend fun saveCredential(vaultItem: VaultItem<SyncObject.Authentifiant>): Boolean

        val sessionEmail: String?
    }

    interface Logger {
        fun display()
        fun onClickSecureButton()
        fun onClickSaveButton()
        fun onCredentialSaved(credential: VaultItem<SyncObject.Authentifiant>)
        fun onClickTryDemo()
        fun onClickReturnHome()
    }
}