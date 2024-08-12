package com.dashlane.item.v3.data

import com.dashlane.item.subview.action.LoginOpener

sealed class ItemAction {
    data object Close : ItemAction()

    data object Saved : ItemAction()

    data object ConfirmSaveChanges : ItemAction()

    data object ConfirmDelete : ItemAction()

    data object OpenShared : ItemAction()

    data object OpenPasswordHistory : ItemAction()

    data object ConfirmRemove2FA : ItemAction()

    data class GuidedPasswordChange(
        val website: String,
        val userName: String?
    ) : ItemAction()

    data class GoToSetup2FA(
        val credentialName: String,
        val credentialId: String,
        val topDomain: String,
        val packageName: String?,
        val proSpace: Boolean
    ) : ItemAction()

    data class OpenPasswordGenerator(val origin: String) : ItemAction()

    data class OpenWebsite(
        val url: String,
        val packageNames: Set<String>,
        val listener: LoginOpener.Listener
    ) : ItemAction()

    data object OpenNoRights : ItemAction()

    data class PasswordRestoreResult(val success: Boolean) : ItemAction()

    data class OpenCollection(
        val temporaryPrivateCollectionsName: List<String>,
        val temporarySharedCollectionsId: List<String>,
        val spaceId: String,
        val isLimited: Boolean
    ) : ItemAction()

    data class OpenLinkedServices(
        val fromViewOnly: Boolean,
        val addNew: Boolean,
        val temporaryWebsites: List<String>,
        val temporaryApps: List<String>?,
        val url: String?
    ) : ItemAction()
}