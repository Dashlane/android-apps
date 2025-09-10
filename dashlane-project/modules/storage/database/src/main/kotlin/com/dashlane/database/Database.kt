package com.dashlane.database

import com.dashlane.vault.summary.groupBySyncObjectType
import com.dashlane.xml.XmlTransaction

interface Database {
    val vaultObjectRepository: VaultObjectRepository

    val backupRepository: BackupRepository

    val memorySummaryRepository: MemorySummaryRepository

    val sharingRepository: SharingRepository

    val searchRepository: SearchRepository
}

fun Database.information(): String {
    return "DATABASE_SUMMARY: ${memorySummaryRepository
        .databaseSummary?.data
        ?.groupBySyncObjectType()
        ?.map { (key, value) -> "$key=${value.size}" }
        ?.joinToString()
    }"
}