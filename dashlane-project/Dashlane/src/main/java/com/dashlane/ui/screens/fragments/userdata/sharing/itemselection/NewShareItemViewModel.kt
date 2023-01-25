package com.dashlane.ui.screens.fragments.userdata.sharing.itemselection

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.loaders.datalists.NewShareItemDataProvider
import com.dashlane.navigation.Navigator
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.useractivity.log.forCode
import com.dashlane.useractivity.log.usage.UsageLogCode80
import com.dashlane.useractivity.log.usage.UsageLogCode86
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewShareItemViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataProvider: NewShareItemDataProvider,
    private val bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>,
    private val sessionManager: SessionManager,
    private val navigator: Navigator
) : ViewModel(), NewShareItemViewModelContract {
    private val usageLogRepository: UsageLogRepository?
        get() = bySessionUsageLogRepository[sessionManager.session]

    private val args = SharingItemSelectionTabFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val from = args.argsUsageLogFrom
    override val ulFrom: UsageLogCode80.From? = UsageLogCode80.From.values().forCode(from)

    override val uiState: MutableStateFlow<NewShareItemViewModelContract.UIState> =
        MutableStateFlow(NewShareItemViewModelContract.UIState.Loading)

    override val selectionState = MutableStateFlow(
        NewShareItemViewModelContract.SelectionState(
            emptyList(), emptyList()
        )
    )

    init {
        viewModelScope.launch {
            dataProvider.init()
            uiState.tryEmit(NewShareItemViewModelContract.UIState.Loading)
            query()
        }
    }

    override fun onQueryChange(query: String): Boolean {
        query(query)
        return true
    }

    override fun onClickNewShare() {
        usageLogRepository
            ?.enqueue(
                UsageLogCode80(
                    type = UsageLogCode80.Type.NEW_SHARE1,
                    from = ulFrom,
                    nbCredentials = selectionState.value.accountsToShare.size,
                    nbSecureNotes = selectionState.value.secureNotesToShare.size,
                    action = UsageLogCode80.Action.NEXT
                )
            )
        navigator.goToPeopleSelectionFromNewShare(
            selectionState.value.accountsToShare.toTypedArray(),
            selectionState.value.secureNotesToShare.toTypedArray()
        )
    }

    override fun onItemSelected(uid: String, type: SyncObjectType) {
        val current = selectionState.value
        val new = if (type == SyncObjectType.AUTHENTIFIANT) {
            current.copy(accountsToShare = current.accountsToShare + uid)
        } else {
            current.copy(secureNotesToShare = current.secureNotesToShare + uid)
        }
        selectionState.tryEmit(new)
    }

    override fun onItemUnSelected(uid: String, type: SyncObjectType) {
        val current = selectionState.value
        val new = if (type == SyncObjectType.AUTHENTIFIANT) {
            current.copy(accountsToShare = current.accountsToShare - uid)
        } else {
            current.copy(secureNotesToShare = current.secureNotesToShare - uid)
        }
        selectionState.tryEmit(new)
    }

    override fun onBackPressed() {
        usageLogRepository?.enqueue(
            UsageLogCode86(
                type = UsageLogCode86.Type.ITEM_SELECT,
                action = UsageLogCode86.Action.BACK
            )
        )
    }

    override fun onCreated() {
        usageLogRepository?.enqueue(
            UsageLogCode80(
                type = UsageLogCode80.Type.NEW_SHARE1,
                action = UsageLogCode80.Action.OPEN,
                from = ulFrom
            )
        )
    }

    fun query(query: String? = null) {
        viewModelScope.launch {
            val d1 = async { dataProvider.loadAccounts(query) }
            val d2 = async { dataProvider.loadSecureNotes(query) }
            uiState.tryEmit(NewShareItemViewModelContract.UIState.Data(d1.await() + d2.await()))
        }
    }
}
