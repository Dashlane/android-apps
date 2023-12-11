package com.dashlane.util.clipboard.vault

import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObjectType
import java.time.LocalDate

sealed class CopyContent {
    sealed class Ready : CopyContent() {
        data class StringValue(val content: String?) : Ready()
        data class ObfuscatedValue(val content: SyncObfuscatedValue?) : Ready()
        data class Date(val content: LocalDate?) : Ready()
        data class YearMonth(val content: java.time.YearMonth?) : Ready()
    }

    data class FromRemoteItem(
        val uid: String,
        val syncObjectType: SyncObjectType,
        val copyField: CopyField
    ) : CopyContent()
}