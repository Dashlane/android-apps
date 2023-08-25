package com.dashlane.storage.userdata.accessor

import com.dashlane.storage.userdata.accessor.filter.CredentialFilter
import com.dashlane.vault.summary.SummaryObject

interface CredentialDataQuery : DataQuery<SummaryObject.Authentifiant, CredentialFilter> {

    fun queryAllPasswords(): List<String>

    fun queryAllUrls(): Set<String>
}