package com.dashlane.autofill.api.emptywebsitewarning.domain

import com.dashlane.autofill.api.emptywebsitewarning.AutofillEmptyWebsiteWarningService
import com.dashlane.autofill.api.emptywebsitewarning.EmptyWebsiteWarningContract
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class EmptyWebsiteWarningDataProvider @Inject constructor(
    private val service: AutofillEmptyWebsiteWarningService
) : EmptyWebsiteWarningContract.DataProvider {

    override suspend fun fetchAccount(uid: String) = service.fetchCredentialFromId(uid)

    override suspend fun updateAccountWithNewUrl(uid: String, newUrl: String): VaultItem<SyncObject.Authentifiant>? =
        service.updateCredentialWebsite(uid, newUrl)
}