package com.dashlane.sharing.itemselection

import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockWatcher
import com.dashlane.sharing.itemselection.NewShareItemSelectionViewModel.Companion.UNLOCK_EVENT_CODE
import com.dashlane.vault.item.VaultListItemStateFactory
import com.dashlane.vault.item.comparator.alphabeticComparator
import com.dashlane.vault.item.header.getAlphabeticalHeader
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.identity.IdentityNameHolderService
import com.dashlane.vault.util.isProtected
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take

fun loadShareableItems(
    newShareItemsRepository: NewShareItemsRepository,
    vaultListItemStateFactory: VaultListItemStateFactory,
    identityNameHolderService: IdentityNameHolderService,
    state: NewShareItemSelectionState.View.Loading,
): Flow<NewShareItemSelectionEvent> {
    return flow {
        val listItemState =
            loadItems(
                newShareItemsRepository = newShareItemsRepository,
                identityNameHolderService = identityNameHolderService,
                vaultListItemStateFactory = vaultListItemStateFactory,
                query = state.data.query,
            )

        emit(
            NewShareItemSelectionEvent.ShareableItemsLoaded(
                listItemState,
                newShareItemsRepository.getSelectedItemCount(),
            ),
        )
    }
}

fun toggleItem(
    newShareItemsRepository: NewShareItemsRepository,
    vaultListItemStateFactory: VaultListItemStateFactory,
    identityNameHolderService: IdentityNameHolderService,
    state: NewShareItemSelectionState.View.ToggleItem,
): Flow<NewShareItemSelectionEvent> {
    return flow {
        val alreadySelected = newShareItemsRepository.isItemSelected(state.itemIdToToggle)
        if (alreadySelected) {
            newShareItemsRepository.unselectItemId(state.itemIdToToggle)
        } else {
            newShareItemsRepository.selectItemId(state.itemIdToToggle)
        }
        val listItemState =
            loadItems(
                newShareItemsRepository = newShareItemsRepository,
                identityNameHolderService = identityNameHolderService,
                vaultListItemStateFactory = vaultListItemStateFactory,
                query = state.data.query,
            )

        emit(
            NewShareItemSelectionEvent.ShareableItemsLoaded(
                listItemState,
                newShareItemsRepository.getSelectedItemCount(),
            ),
        )
    }
}

fun checkProtectedItems(
    newShareItemsRepository: NewShareItemsRepository,
    lockWatcher: LockWatcher,
    state: NewShareItemSelectionState.View.UnlockProtectedItems,
): Flow<NewShareItemSelectionEvent> {
    return if (newShareItemsRepository.hasProtectedItemsSelected()) {
        lockWatcher.lockEventFlow
            .take(1)
            .map {
                if (
                    it is LockEvent.Unlock &&
                    it.reason is LockEvent.Unlock.Reason.WithCode &&
                    (it.reason as LockEvent.Unlock.Reason.WithCode).requestCode == UNLOCK_EVENT_CODE
                ) {
                    val selectedItems = newShareItemsRepository.getSelectedItemsIdsByType()
                    NewShareItemSelectionEvent.Proceed(
                        selectedItems[SyncObjectType.AUTHENTIFIANT] ?: emptyList(),
                        selectedItems[SyncObjectType.SECURE_NOTE] ?: emptyList(),
                        selectedItems[SyncObjectType.SECRET] ?: emptyList(),
                    )
                } else {
                    NewShareItemSelectionEvent.UnlockCancelled
                }
            }.onStart {
                emit(NewShareItemSelectionEvent.UnlockRequired)
            }
    } else {
        flow {
            val selectedItems = newShareItemsRepository.getSelectedItemsIdsByType()
            emit(
                NewShareItemSelectionEvent.Proceed(
                    selectedItems[SyncObjectType.AUTHENTIFIANT] ?: emptyList(),
                    selectedItems[SyncObjectType.SECURE_NOTE] ?: emptyList(),
                    selectedItems[SyncObjectType.SECRET] ?: emptyList(),
                ),
            )
        }
    }
}

private suspend fun loadItems(
    newShareItemsRepository: NewShareItemsRepository,
    identityNameHolderService: IdentityNameHolderService,
    vaultListItemStateFactory: VaultListItemStateFactory,
    query: String?,
): List<ListItemState> {
    val shareableSummaries = newShareItemsRepository.fetchShareableSummaries(
        query = query,
        comparator = alphabeticComparator(identityNameHolderService),
    )

    val groupedSummaries: Map<String, List<SummaryObject>> =
        shareableSummaries.groupBy { it.getAlphabeticalHeader(identityNameHolderService) }

    val listItemState = groupedSummaries.map { (header, summaries) ->
        buildList {
            add(ListItemState.HeaderItemState(title = header))
            addAll(
                summaries.map { summary ->
                    ListItemState.VaultItemState(
                        vaultListItemState = vaultListItemStateFactory.buildVaultListItemState(summary).copy(
                            isLockIconVisible = summary.isProtected,
                            isChecked = newShareItemsRepository.isItemSelected(summary.id),
                            isSharedIconVisible = summary.isShared,
                        ),
                    )
                },
            )
        }
    }.flatten()
    return listItemState
}