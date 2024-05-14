package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

val VaultItem<*>.isLocked get() = (syncObject as? SyncObject.SecureNote)?.secured ?: false

fun VaultItem<*>.copyWithLock(newLock: Boolean) = if (syncObject is SyncObject.SecureNote) {
    copy(syncObject = (syncObject as SyncObject.SecureNote).copy { secured = newLock })
} else {
    this
}