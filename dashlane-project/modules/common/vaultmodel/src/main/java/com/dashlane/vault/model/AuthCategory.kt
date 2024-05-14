package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun VaultItem<SyncObject.AuthCategory>.copySyncObject(builder: SyncObject.AuthCategory.Builder.() -> Unit = {}):
        VaultItem<SyncObject.AuthCategory> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
