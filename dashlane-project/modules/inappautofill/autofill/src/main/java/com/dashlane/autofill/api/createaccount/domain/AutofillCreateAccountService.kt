package com.dashlane.autofill.api.createaccount.domain

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject



interface AutofillCreateAccountService {

    

    suspend fun saveNewAuthentifiant(credential: CredentialInfo): VaultItem<SyncObject.Authentifiant>?

    

    fun loadExistingLogins(): List<String>

    

    fun getFamousWebsitesList(): List<String>
}