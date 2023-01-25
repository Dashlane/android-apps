package com.dashlane.vault.model

import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.get
import com.dashlane.vault.util.isSpaceSupported
import com.dashlane.xml.domain.SyncObjectType

fun VaultItem<*>.isSpaceItem() = SyncObjectType[this]?.isSpaceSupported == true

fun VaultItem<*>.copyWithSpaceId(newSpaceId: String?) = copyWithAttrs { teamSpaceId = newSpaceId }

fun SummaryObject.isSpaceItem() = SyncObjectType[this]?.isSpaceSupported == true
