package com.dashlane.ui.screens.sharing

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.core.domain.sharing.toSharingPermission
import com.dashlane.logger.Log
import com.dashlane.logger.v
import com.dashlane.navigation.Navigator
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import com.dashlane.sharing.exception.SharingAlreadyAccessException
import com.dashlane.useractivity.SharingDeveloperLogger
import com.dashlane.useractivity.log.forCode
import com.dashlane.useractivity.log.usage.UsageLogCode80
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewSharePeopleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dataProvider: NewSharePeopleDataProvider,
    private val navigator: Navigator,
    private val sharingLogger: SharingLogger,
    private val sharingDeveloperLogger: SharingDeveloperLogger
) : ViewModel(), NewSharePeopleViewModelContract {
    private val args = SharingNewSharePeopleFragmentArgs.fromSavedStateHandle(savedStateHandle)
    private val authentifiantUIDs = args.argsAuthentifiantUIDs
    private val secureNotesUIDs = args.argsSecureNotesUIDs
    private val from = args.from ?: ""

    private val ul80From: UsageLogCode80.From? = UsageLogCode80.From.values().forCode(from)

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

    override fun onBackPressed(contacts: List<SharingContact>) {
        sharingLogger.logNewShareStep2Back(
            from = ul80From,
            accountSize = authentifiantUIDs.size,
            secureNoteSize = secureNotesUIDs.size,
            emailSize = contacts.size,
            permission = permission.value.toSharingPermission()
        )
    }

    override fun onClickShare(contacts: List<SharingContact>) {
        if (authentifiantUIDs.isEmpty() && secureNotesUIDs.isEmpty() && contacts.isEmpty()) {
            uiState.tryEmit(NewSharePeopleViewModelContract.UIState.ERROR)
            return
        }

        sharingLogger.logNewShareStep2Next(
            from = ul80From,
            accountSize = authentifiantUIDs.size,
            secureNoteSize = secureNotesUIDs.size,
            emailSize = contacts.size,
            permission = permission.value.toSharingPermission()
        )
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
                Log.v(it)
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
                if (UsageLogCode80.From.CREDENTIALS == ul80From || UsageLogCode80.From.SECURE_NOTES == ul80From) {
                    uiState.tryEmit(NewSharePeopleViewModelContract.UIState.SUCCESS_FOR_RESULT)
                } else {
                    navigator.goToPasswordSharingFromPeopleSelection()
                }
            }
        }
    }

    private suspend fun buildContactList(): List<SharingContact> = dataProvider.load()
}