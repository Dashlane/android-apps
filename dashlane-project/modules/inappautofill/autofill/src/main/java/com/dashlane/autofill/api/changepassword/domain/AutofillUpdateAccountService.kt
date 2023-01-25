package com.dashlane.autofill.api.changepassword.domain

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject



interface AutofillUpdateAccountService {

    

    suspend fun updateExistingAuthentifiant(credential: CredentialUpdateInfo): VaultItem<SyncObject.Authentifiant>?

    suspend fun loadAuthentifiants(
        website: String? = null,
        packageName: String? = null
    ): List<SyncObject.Authentifiant>
}