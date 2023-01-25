package com.dashlane.ui.screens.sharing

import android.os.Parcelable
import com.dashlane.server.api.endpoints.sharinguserdevice.Permission
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.parcelize.Parcelize

interface NewSharePeopleViewModelContract {
    val uiState: Flow<UIState>
    val contacts: Flow<List<SharingContact>>
    val permission: StateFlow<Permission>
    fun onPermissionChanged(sharingPermission: Permission)
    fun onClickShare(contacts: List<SharingContact>)
    fun onBackPressed(contacts: List<SharingContact>)

    enum class UIState {
        INIT,
        LOADING,
        ERROR,
        ERROR_ALREADY_ACCESS,
        ERROR_FIND_USERS,
        SUCCESS,
        SUCCESS_FOR_RESULT,
    }
}

sealed class SharingContact : Parcelable {
    @Parcelize
    data class SharingContactUser(val name: String) : SharingContact() {
        override fun toString(): String = name
    }

    @Parcelize
    data class SharingContactUserGroup(val id: String, val name: String) : SharingContact() {
        override fun toString(): String = name
    }
}