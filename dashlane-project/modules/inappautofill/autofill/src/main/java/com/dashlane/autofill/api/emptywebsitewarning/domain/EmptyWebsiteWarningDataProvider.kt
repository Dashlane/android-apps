package com.dashlane.autofill.api.emptywebsitewarning.domain

import com.dashlane.autofill.api.emptywebsitewarning.AutofillEmptyWebsiteWarningService
import com.dashlane.autofill.api.emptywebsitewarning.EmptyWebsiteWarningContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EmptyWebsiteWarningDataProvider @Inject constructor(
    private val service: AutofillEmptyWebsiteWarningService
) : EmptyWebsiteWarningContract.DataProvider {

    override suspend fun fetchAccount(uid: String) = withContext(Dispatchers.IO) {
        service.fetchCredentialFromId(uid)
    }

    override suspend fun updateAccountWithNewUrl(uid: String, newUrl: String) = withContext(Dispatchers.IO) {
        service.updateCredentialWebsite(uid, newUrl)
    }
}