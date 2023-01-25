package com.dashlane.item.linkedwebsites

import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject

data class LinkedServicesUIState(
    val vaultItem: VaultItem<SyncObject.Authentifiant>?,
    val editMode: Boolean,
    val actionClosePageAfterSave: Boolean,
    val closePageImmediate: Boolean
)