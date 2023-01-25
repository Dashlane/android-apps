package com.dashlane.autofill.api.changepassword

import com.dashlane.autofill.api.changepassword.domain.CredentialUpdateInfo
import com.dashlane.autofill.api.common.GeneratePasswordContract
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject



class ChangePasswordContract {

    interface Presenter : GeneratePasswordContract.Presenter {
        

        fun useButtonClicked()

        

        fun initDialog()

        

        fun onCancel()
    }

    interface ViewProxy : GeneratePasswordContract.ViewProxy {
        

        fun getFilledData(): FilledData

        

        fun prefillLogin(logins: List<String>)

        

        fun enableUse(enable: Boolean)

        

        fun displayError(message: String)
    }

    interface DataProvider : GeneratePasswordContract.DataProvider {

        suspend fun loadAuthentifiants(): List<SyncObject.Authentifiant>

        

        suspend fun updateCredentialToVault(credential: CredentialUpdateInfo): VaultItem<SyncObject.Authentifiant>?

        

        fun getCredential(login: String): SyncObject.Authentifiant
    }
}