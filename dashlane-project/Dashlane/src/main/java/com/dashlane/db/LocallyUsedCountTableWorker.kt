package com.dashlane.db

import androidx.annotation.VisibleForTesting
import com.dashlane.database.ISQLiteDatabase
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.storage.tableworker.DatabaseTableWorker
import com.dashlane.vault.model.toSql
import com.dashlane.xml.domain.SyncObjectType

class LocallyUsedCountTableWorker : DatabaseTableWorker() {

    override fun updateDatabaseTables(db: ISQLiteDatabase, oldVersion: Int, newVersion: Int): Boolean {
        if (oldVersion < LOCALLY_USED_COUNT_SUPPORT_VERSION) {
            addLocallyUsedCount(db)
        }
        return true
    }

    override fun createDatabaseTables(db: ISQLiteDatabase?) {
        
    }

    @VisibleForTesting
    fun addLocallyUsedCount(db: ISQLiteDatabase) {
        val dataIdentifierTables = enumValues<SyncObjectType>().mapNotNull { it.toSql()?.tableName }

        for (table in dataIdentifierTables) {
            if (!checkIfColumnExists(db, table, DataIdentifierSql.FIELD_LOCALLY_USED_COUNT)) {
                addLocallyUsedCount(db, table)
            }
        }
    }

    @VisibleForTesting
    fun addLocallyUsedCount(db: ISQLiteDatabase, tableName: String) {
        
        val locallyUsedCount = DataIdentifierSql.FIELD_LOCALLY_USED_COUNT
        val sql = getSqlAddColumn(tableName, locallyUsedCount, "LONG DEFAULT 0;")
        db.execSQL(sql)
    }

    companion object {
        private const val LOCALLY_USED_COUNT_SUPPORT_VERSION = 42
    }
}