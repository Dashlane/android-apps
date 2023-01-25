package com.dashlane.db

import com.dashlane.database.ISQLiteDatabase
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.database.sql.GeneratedPasswordSql
import com.dashlane.database.sql.SecureNoteCategorySql
import com.dashlane.storage.tableworker.DatabaseTableWorker



class SchemaConsistencyTableWorker : DatabaseTableWorker() {
    override fun updateDatabaseTables(db: ISQLiteDatabase, oldVersion: Int, newVersion: Int): Boolean {
        if (oldVersion < 37) {
            
            val tables = listOf(
                GeneratedPasswordSql.TABLE_NAME,
                SecureNoteCategorySql.TABLE_NAME
            )
            for (table in tables) {
                addAttachmentsColumn(db, table)
            }

            
            val obsoleteTables = setOf(
                "A",
                "BREACHNOTIFICATION",
                "DATAUSAGEHISTORY",
                "DATAUSAGEHISTORYBUFFER",
                "SHAREDITEM",
                "WEBSITE"
            )
            for (table in obsoleteTables) {
                db.rawExecSQL("DROP TABLE IF EXISTS $table")
            }
        }

        return true
    }

    private fun addAttachmentsColumn(db: ISQLiteDatabase, table: String) {
        if (!checkIfColumnExists(db, table, DataIdentifierSql.FIELD_ATTACHMENTS)) {
            val sql = getSqlAddColumn(
                table,
                DataIdentifierSql.FIELD_ATTACHMENTS,
                " TEXT DEFAULT '';"
            )
            db.execSQL(sql)
        }
    }

    override fun createDatabaseTables(db: ISQLiteDatabase?) {
        
    }
}