package com.dashlane.autofill.phishing

import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@ActivityRetainedScoped
class PhishingWarningRepository @Inject constructor() : Mutex by Mutex() {
    private var data: Pair<WebDomainFormSource, SummaryObject.Authentifiant>? = null

    @SuppressWarnings("kotlin:S6512") 
    suspend fun getData(): Pair<WebDomainFormSource, SummaryObject.Authentifiant>? =
        withLock { data }

    suspend fun updateData(
        summaryItem: SummaryObject.Authentifiant,
        webFormSource: WebDomainFormSource,
    ) = withLock { this.data = webFormSource to summaryItem }
}