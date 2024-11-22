package com.dashlane.item.v3.loaders.common

import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.repositories.CollectionsRepository
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import javax.inject.Inject
import kotlin.reflect.KFunction1

class CollectionLoader<T : FormData> @Inject constructor(
    private val collectionsRepository: CollectionsRepository
) {
    fun loadAsync(
        scope: CoroutineScope,
        initialSummaryObject: SummaryObject,
        additionalDataLoadedFunction: KFunction1<(Data<T>) -> Data<T>, Unit>
    ): Deferred<Unit> = scope.async(start = CoroutineStart.LAZY) {
        val collections = collectionsRepository.getCollections(initialSummaryObject)
        additionalDataLoadedFunction {
            it.copyCommonData {
                it.copy(collections = collections)
            }
        }
    }
}