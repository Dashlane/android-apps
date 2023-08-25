package com.dashlane.ui.screens.sharing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.navigation.Navigator
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.sharing.exception.SharingAlreadyAccessException
import com.dashlane.useractivity.SharingDeveloperLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

@HiltViewModel
class NewSharePeopleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataProvider: NewSharePeopleDataProvider,
    private val navigator: Navigator,
    private val sharingDeveloperLogger: SharingDeveloperLogger
) : ViewModel(), NewSharePeopleViewModelContract {
    private val args = SharingNewSharePeopleFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val authentifiantUIDs = args.argsAuthentifiantUIDs
    private val secureNotesUIDs = args.argsSecureNotesUIDs

    override val uiState = MutableStateFlow(NewSharePeopleViewModelContract.UIState.INIT)
    override val permission = MutableStateFlow(Permission.LIMITED)

    override val contacts =
        flow {
            val checklistData = buildContactList()
            emit(checklistData)
        }.shareIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            replay = 1
        )

    override fun onPermissionChanged(sharingPermission: Permission) {
        permission.tryEmit(sharingPermission)
    }

    override fun onClickShare(contacts: List<SharingContact>) {
        if (authentifiantUIDs.isEmpty() && secureNotesUIDs.isEmpty() && contacts.isEmpty()) {
            uiState.tryEmit(NewSharePeopleViewModelContract.UIState.ERROR)
            return
        }

        sharingDeveloperLogger.newShareAttempt()

        viewModelScope.launch {
            uiState.tryEmit(NewSharePeopleViewModelContract.UIState.LOADING)
            runCatching {
                dataProvider.share(
                    authentifiantUIDs,
                    secureNotesUIDs,
                    contacts,
                    permission.value
                )
            }.onFailure {
                sharingDeveloperLogger.newShareFailure(it)
                when (it) {
                    is SharingAlreadyAccessException -> {
                        uiState.tryEmit(NewSharePeopleViewModelContract.UIState.ERROR_ALREADY_ACCESS)
                    }

                    else -> {
                        uiState.tryEmit(NewSharePeopleViewModelContract.UIState.ERROR)
                    }
                }
            }.onSuccess {
                sharingDeveloperLogger.newShareSuccess()
                uiState.tryEmit(NewSharePeopleViewModelContract.UIState.SUCCESS)
                if (SharingNewSharePeopleFragment.FROM_ITEM_VIEW == args.from) {
                    uiState.tryEmit(NewSharePeopleViewModelContract.UIState.SUCCESS_FOR_RESULT)
                } else {
                    navigator.popBackStack()
                    navigator.goToPasswordSharingFromPeopleSelection()
                }
            }
        }
    }

    private suspend fun buildContactList(): List<SharingContact> = dataProvider.load()
}
