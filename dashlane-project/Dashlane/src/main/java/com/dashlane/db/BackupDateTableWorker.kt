package com.dashlane.db

import com.dashlane.database.ISQLiteDatabase
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.storage.tableworker.DatabaseTableWorker
import com.dashlane.vault.model.toSql
import com.dashlane.xml.domain.SyncObjectType



class BackupDateTableWorker : DatabaseTableWorker() {

    override fun updateDatabaseTables(db: ISQLiteDatabase, oldVersion: Int, newVersion: Int): Boolean {
        if (oldVersion < BACKUP_DATE_SUPPORT_VERSION_RETRY) {
            addBackupDateIfNotExists(db)
        }

        return true
    }

    private fun addBackupDateIfNotExists(db: ISQLiteDatabase) {
        val dataIdentifierTables = enumValues<SyncObjectType>().mapNotNull { it.toSql()?.tableName }
        for (table in dataIdentifierTables) {
            if (!checkIfColumnExists(db, table, DataIdentifierSql.FIELD_BACKUP_DATE)) {
                addBackupDate(db, table)
            }
        }
    }

    private fun addBackupDate(db: ISQLiteDatabase, tableName: String) {
        
        val sql = getSqlAddColumn(
            tableName,
            DataIdentifierSql.FIELD_BACKUP_DATE,
            "INTEGER DEFAULT 0;"
        )
        db.execSQL(sql)
    }

    override fun createDatabaseTables(db: ISQLiteDatabase) {
        
    }

    companion object {
        private const val BACKUP_DATE_SUPPORT_VERSION_RETRY = 48
    }
}