package com.dashlane.autofill.api.rememberaccount.linkedservices

import com.dashlane.autofill.api.rememberaccount.services.FormSourceAuthentifiantLinker
import com.dashlane.autofill.core.AutoFillDataBaseAccess
import com.dashlane.util.matchDomain
import javax.inject.Inject

class WebDomainFormSourceAuthentifiantLinker @Inject constructor(private val autoFillDataBaseAccess: AutoFillDataBaseAccess) :
    FormSourceAuthentifiantLinker {

    

    override suspend fun isLinked(formSourceIdentifier: String, authentifiantId: String): Boolean {
        return autoFillDataBaseAccess.loadSummaryAuthentifiant(authentifiantId)?.let { summary ->
            summary.linkedServices?.associatedDomains?.any { it.domain.matchDomain(formSourceIdentifier) }
        } ?: false
    }

    override suspend fun link(formSourceIdentifier: String, authentifiantId: String): Boolean =
        autoFillDataBaseAccess.addAuthentifiantLinkedWebDomain(authentifiantId, formSourceIdentifier)
}