package com.dashlane.autofill.changepassword

import com.dashlane.autofill.changepassword.domain.CredentialUpdateInfo
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

class ChangePasswordContract {

    interface ViewProxy {
        fun prefillLogin(logins: List<String>)

        fun enableUse(enable: Boolean)

        fun displayError(message: String)
    }

    interface DataProvider {

        suspend fun loadAuthentifiants(website: String?, packageName: String?): List<VaultItem<SyncObject.Authentifiant>>

        suspend fun updateCredentialToVault(credential: CredentialUpdateInfo): VaultItem<SyncObject.Authentifiant>?

        fun getCredential(login: String): VaultItem<SyncObject.Authentifiant>
    }
}