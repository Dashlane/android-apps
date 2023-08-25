package com.dashlane.autofill.api.rememberaccount.linkedservices

import com.dashlane.autofill.api.rememberaccount.services.FormSourceAuthentifiantLinker
import com.dashlane.autofill.core.AutoFillDataBaseAccess
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class ApplicationFormSourceAuthentifiantLinker @Inject constructor(
    private val autoFillDataBaseAccess: AutoFillDataBaseAccess
) : FormSourceAuthentifiantLinker {

    override suspend fun isLinked(formSourceIdentifier: String, authentifiantId: String): Boolean {
        return autoFillDataBaseAccess.loadSummary<SummaryObject.Authentifiant>(authentifiantId)?.let { summary ->
            summary.linkedServices?.associatedAndroidApps?.any { it.packageName == formSourceIdentifier }
        } ?: false
    }

    override suspend fun link(formSourceIdentifier: String, authentifiantId: String): Boolean =
        autoFillDataBaseAccess.addAuthentifiantLinkedApp(authentifiantId, formSourceIdentifier)
}