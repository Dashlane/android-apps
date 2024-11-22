package com.dashlane.item.v3.loaders

import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.loaders.common.CollectionLoader
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject
import kotlin.reflect.KFunction1
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class SecureNoteAsyncDataLoader @Inject constructor(
    private val collectionLoader: CollectionLoader<SecureNoteFormData>,
    private val sharingPolicyDataProvider: SharingPolicyDataProvider
) : AsyncDataLoader<SecureNoteFormData> {
    private val additionalAsyncData: MutableList<Deferred<Unit>> = mutableListOf()

    override fun loadAsync(
        initialSummaryObject: SummaryObject,
        isNewItem: Boolean,
        scope: CoroutineScope,
        additionalDataLoadedFunction: KFunction1<(Data<SecureNoteFormData>) -> Data<SecureNoteFormData>, Unit>,
        onAllDataLoaded: suspend () -> Unit
    ) {
        additionalAsyncData.add(collectionLoader.loadAsync(scope, initialSummaryObject, additionalDataLoadedFunction))

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
        additionalAsyncData.add(
            scope.async(start = CoroutineStart.LAZY) {
                val sharingCount =
                    sharingPolicyDataProvider.getSharingCount(initialSummaryObject.id)
                additionalDataLoadedFunction {
                    it.updateCommonData {
                        it.copy(sharingCount = FormData.SharingCount(sharingCount))
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