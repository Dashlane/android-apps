package com.dashlane.item.v3.loaders

import com.dashlane.item.v3.ItemEditFragmentArgs
import com.dashlane.item.v3.data.SecretFormData
import com.dashlane.item.v3.loaders.common.CanDeleteLoader
import com.dashlane.item.v3.loaders.common.CollectionLoader
import com.dashlane.item.v3.loaders.common.SharingCountLoader
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.teamspaces.db.SmartSpaceItemChecker
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
    private val canDeleteLoader: CanDeleteLoader<SecretFormData>,
    private val sharingCountLoader: SharingCountLoader<SecretFormData>,
    private val smartSpaceItemChecker: SmartSpaceItemChecker,
) : AsyncDataLoader<SecretFormData> {
    override val collectionLoader: CollectionLoader<SecretFormData>? = null

    private val additionalAsyncData: MutableList<Deferred<Unit>> = mutableListOf()

    override fun loadAsync(
        initialSummaryObject: SummaryObject,
        isNewItem: Boolean,
        navArgs: ItemEditFragmentArgs,
        scope: CoroutineScope,
        additionalDataLoadedFunction: KFunction1<(Data<SecretFormData>) -> Data<SecretFormData>, Unit>,
        onAllDataLoaded: suspend () -> Unit,
    ) {
        additionalAsyncData.add(canDeleteLoader.loadAsync(scope, initialSummaryObject, isNewItem, additionalDataLoadedFunction))
        additionalAsyncData.add(sharingCountLoader.loadAsync(scope, initialSummaryObject, additionalDataLoadedFunction))

        additionalAsyncData.add(
            scope.async(start = CoroutineStart.LAZY) {
                val isForcedSpace = smartSpaceItemChecker.checkForceSpace(initialSummaryObject)
                additionalDataLoadedFunction {
                    it.updateCommonData { it.copy(isForcedSpace = isForcedSpace) }
                }
            },
        )

        scope.launch {
            additionalAsyncData.joinAll()
            onAllDataLoaded()
        }
    }

    override fun cancelAll() = additionalAsyncData.forEach { it.cancel() }
}