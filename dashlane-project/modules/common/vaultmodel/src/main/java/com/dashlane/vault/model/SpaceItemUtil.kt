package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun <T : SyncObject> VaultItem<T>.copyWithSpaceId(newSpaceId: String?) = copyWithAttrs { teamSpaceId = newSpaceId }
