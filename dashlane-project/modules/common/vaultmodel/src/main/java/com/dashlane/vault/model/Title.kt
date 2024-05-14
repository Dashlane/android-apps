package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun SyncObject.Identity.Title.getStringId(): Int {
    return when (this) {
        SyncObject.Identity.Title.MME -> R.string.title_mrs
        SyncObject.Identity.Title.MR -> R.string.title_mr
        SyncObject.Identity.Title.MS -> R.string.title_ms
        SyncObject.Identity.Title.MX -> R.string.title_mx
        SyncObject.Identity.Title.MLLE -> R.string.title_miss
        SyncObject.Identity.Title.NONE_OF_THESE -> R.string.title_none_of_these
    }
}
