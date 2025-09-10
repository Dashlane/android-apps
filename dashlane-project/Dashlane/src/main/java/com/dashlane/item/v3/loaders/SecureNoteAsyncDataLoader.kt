package com.dashlane.item.v3.loaders

import com.dashlane.item.v3.ItemEditFragmentArgs
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.loaders.common.CanDeleteLoader
import com.dashlane.item.v3.loaders.common.CollectionLoader
import com.dashlane.item.v3.loaders.common.SharingCountLoader
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.KFunction1

class SecureNoteAsyncDataLoader @Inject constructor(
    override val collectionLoader: CollectionLoader<SecureNoteFormData>,
    private val canDeleteLoader: CanDeleteLoader<SecureNoteFormData>,
    private val sharingCountLoader: SharingCountLoader<SecureNoteFormData>,
) : AsyncDataLoader<SecureNoteFormData> {
    private val additionalAsyncData: MutableList<Deferred<Unit>> = mutableListOf()

    override fun loadAsync(
        initialSummaryObject: SummaryObject,
        isNewItem: Boolean,
        navArgs: ItemEditFragmentArgs,
        scope: CoroutineScope,
        additionalDataLoadedFunction: KFunction1<(Data<SecureNoteFormData>) -> Data<SecureNoteFormData>, Unit>,
        onAllDataLoaded: suspend () -> Unit,
    ) {
        additionalAsyncData.add(collectionLoader.loadAsync(scope, initialSummaryObject, additionalDataLoadedFunction))
        additionalAsyncData.add(canDeleteLoader.loadAsync(scope, initialSummaryObject, isNewItem, additionalDataLoadedFunction))
        additionalAsyncData.add(sharingCountLoader.loadAsync(scope, initialSummaryObject, additionalDataLoadedFunction))

        scope.launch {
            additionalAsyncData.joinAll()
            onAllDataLoaded()
        }
    }

    override fun cancelAll() = additionalAsyncData.forEach { it.cancel() }
}