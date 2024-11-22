package com.dashlane.item.v3.loaders

import com.dashlane.item.v3.data.SecretFormData
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KFunction1

class SecretAsyncDataLoader @Inject constructor(
    private val sharingPolicyDataProvider: SharingPolicyDataProvider
) : AsyncDataLoader<SecretFormData> {
    private val additionalAsyncData: MutableList<Deferred<Unit>> = mutableListOf()

    override fun loadAsync(
        initialSummaryObject: SummaryObject,
        isNewItem: Boolean,
        scope: CoroutineScope,
        additionalDataLoadedFunction: KFunction1<(Data<SecretFormData>) -> Data<SecretFormData>, Unit>,
        onAllDataLoaded: suspend () -> Unit
    ) {
        additionalAsyncData.add(
            scope.async(start = CoroutineStart.LAZY) {
                val canDelete = sharingPolicyDataProvider.isDeleteAllowed(
                    initialSummaryObject.id,
                    isNewItem,
                    initialSummaryObject.isShared
                )
                additionalDataLoadedFunction {
                    it.updateCommonData {
                        it.copy(canDelete = canDelete)
                    }
                }
            }
        )

        scope.launch {
            additionalAsyncData.joinAll()
            onAllDataLoaded()
        }
    }

    override fun cancelAll() = additionalAsyncData.forEach { it.cancel() }
}