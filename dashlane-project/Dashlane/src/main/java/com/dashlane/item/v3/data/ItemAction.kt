package com.dashlane.item.v3.data

import com.dashlane.mvvm.State

sealed class ItemAction : State.SideEffect {
    data object Close : ItemAction()

    data object SaveError : ItemAction()

    data object ConfirmSaveChanges : ItemAction()

    data object ConfirmRemove2FA : ItemAction()

    data object OpenNoRights : ItemAction()
}