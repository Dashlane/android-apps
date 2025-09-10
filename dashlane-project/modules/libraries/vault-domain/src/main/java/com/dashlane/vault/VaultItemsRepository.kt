package com.dashlane.vault

import com.dashlane.data.login.LoginInfo
import com.dashlane.hermes.generated.definitions.Trigger
import com.dashlane.session.Session
import com.dashlane.session.SessionObserver
import com.dashlane.session.repository.SessionCoroutineScopeRepository
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.sync.DataSync
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import com.dashlane.vault.summary.SummaryObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class VaultItemsRepository @Inject constructor(
    private val genericDataQuery: GenericDataQuery,
    private val dataSync: DataSync,
    private val sessionCoroutineScopeRepository: SessionCoroutineScopeRepository,
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher,
    private val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter,
) : SessionObserver {
    internal val channel = Channel<Unit>(onBufferOverflow = BufferOverflow.DROP_LATEST)
    internal val itemsFlow: MutableSharedFlow<List<SummaryObject>> = MutableSharedFlow(replay = 1)
    val vaultItems: SharedFlow<List<SummaryObject>> = itemsFlow.asSharedFlow()

    override suspend fun sessionStarted(session: Session, loginInfo: LoginInfo?) {
        sessionCoroutineScopeRepository[session]
            ?.let { scope ->
                channel.receiveAsFlow()
                    .onEach {
                        val items = queryData()
                        itemsFlow.emit(items)
                    }.launchIn(scope)
            }
    }

    override suspend fun sessionEnded(session: Session, byUser: Boolean, forceLogout: Boolean) {
        itemsFlow.resetReplayCache()
    }

    suspend fun loadVault() {
        channel.send(Unit)
    }

    fun refresh() {
        dataSync.sync(Trigger.MANUAL)
    }

    private suspend fun queryData(): List<SummaryObject> = withContext(ioDispatcher) {
        
        launch {
            runCatching {
                val space = currentTeamSpaceUiFilter.currentFilter.teamSpace
                    tag = "VaultItemsRepository",
                    message = "current space: $space (spaceId: '${space.teamId}' null=${space.teamId == null})",
                )
                val allItemsWithoutSpaceFilter = genericDataQuery.queryAll(
                    genericFilter {
                        specificDataType(Filter.ALL_VISIBLE_VAULT_ITEM_TYPES)
                    },
                )

                allItemsWithoutSpaceFilter.logVaultList("all items without space filter")
            }
        }

        val genericFilter = genericFilter {
            specificDataType(Filter.ALL_VISIBLE_VAULT_ITEM_TYPES)
            forCurrentSpace()
        }
        genericDataQuery.queryAll(genericFilter)
            .also { it.logVaultList("all items with space filter") }
    }

    private fun List<SummaryObject>.logVaultList(context: String) {
            tag = "VaultItemsRepository",
            message = "queryData: $context: $size",
        )

        groupBy { it.syncObjectType }
            .forEach { (syncObjectType, items) ->
                val spaces = items.groupBy { it.spaceId }.map {
                    if (it.key == null) {
                        it.key
                    } else {
                        "\"${it.key}\""
                    }
                }
                    tag = "VaultItemsRepository",
                    message = "queryData: $context: $syncObjectType: ${items.size} with spaces: $spaces",
                )
            }
    }
}