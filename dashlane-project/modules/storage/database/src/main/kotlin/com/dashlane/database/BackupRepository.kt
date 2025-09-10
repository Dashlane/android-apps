package com.dashlane.database

import com.dashlane.xml.XmlTransaction

interface BackupRepository {
    suspend fun save(id: Id, backup: XmlTransaction)

    suspend fun load(id: Id): XmlTransaction?
}