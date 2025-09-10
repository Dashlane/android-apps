package com.dashlane.item.v3.loaders.common

import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import javax.inject.Inject
import kotlin.reflect.KFunction1

class SharingCountLoader<T : FormData> @Inject constructor(
    private val sharingPolicyDataProvider: SharingPolicyDataProvider,
) {
    fun loadAsync(
        scope: CoroutineScope,
        initialSummaryObject: SummaryObject,
        additionalDataLoadedFunction: KFunction1<(Data<T>) -> Data<T>, Unit>,
    ): Deferred<Unit> = scope.async(start = CoroutineStart.LAZY) {
        val sharingCount = sharingPolicyDataProvider.getSharingCount(initialSummaryObject.id)
        additionalDataLoadedFunction {
            it.copyCommonData {
                it.copy(sharingCount = FormData.SharingCount(sharingCount))
            }
        }
    }
}