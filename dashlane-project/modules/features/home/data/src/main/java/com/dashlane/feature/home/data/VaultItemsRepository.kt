package com.dashlane.feature.home.data

import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.sync.DataSync
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultItemsRepository @Inject constructor(
    private val genericDataQuery: GenericDataQuery,
    private val dataSync: DataSync,
    @ApplicationCoroutineScope
    private val scope: CoroutineScope,
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val channel = Channel<Unit>(onBufferOverflow = BufferOverflow.DROP_LATEST)
    private val itemsFlow: MutableSharedFlow<List<SummaryObject>> = MutableSharedFlow(replay = 1)
    val vaultItems: SharedFlow<List<SummaryObject>> = itemsFlow.asSharedFlow()

    init {
        channel.receiveAsFlow()
            .onEach {
                val items = queryData()
                itemsFlow.emit(items)
            }.launchIn(scope)
    }

    suspend fun loadVault() {
        channel.send(Unit)
    }

    fun refresh() {
        dataSync.sync(Trigger.MANUAL)
    }

    private suspend fun queryData() = withContext(ioDispatcher) {
        val genericFilter = genericFilter {
            specificDataType(Filter.ALL_VISIBLE_VAULT_ITEM_TYPES)
            forCurrentSpace()
        }
        genericDataQuery.queryAll(genericFilter)
    }
}