package com.dashlane.vault.model

import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.isSpaceSupported

fun VaultItem<*>.isSpaceItem() = syncObjectType.isSpaceSupported

fun VaultItem<*>.copyWithSpaceId(newSpaceId: String?) = copyWithAttrs { teamSpaceId = newSpaceId }

fun SummaryObject.isSpaceItem() = syncObjectType.isSpaceSupported
