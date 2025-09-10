package com.dashlane.autofill.linkservices.rememberaccount

import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.vault.summary.SummaryObject
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@ActivityRetainedScoped
class RememberAccountRepository @Inject constructor() : Mutex by Mutex() {
    private var data: Pair<AutoFillFormSource, SummaryObject.Authentifiant>? = null

    @SuppressWarnings("kotlin:S6512") 
    suspend fun getData(): Pair<AutoFillFormSource, SummaryObject.Authentifiant>? =
        withLock { data }

    suspend fun updateData(
        formSource: AutoFillFormSource,
        summaryItem: SummaryObject.Authentifiant,
    ) =
        withLock { this.data = formSource to summaryItem }
}