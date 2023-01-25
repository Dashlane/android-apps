package com.dashlane.db

import com.dashlane.database.ISQLiteDatabase
import com.dashlane.database.sql.AuthentifiantSql
import com.dashlane.storage.tableworker.DatabaseTableWorker

class LinkedServicesDatabaseTableWorker : DatabaseTableWorker() {
    override fun updateDatabaseTables(db: ISQLiteDatabase, oldVersion: Int, newVersion: Int): Boolean {
        if (oldVersion < 47) {
            addMissingField(db)
        }
        return true
    }

    override fun createDatabaseTables(db: ISQLiteDatabase?) {
        
    }

    private fun addMissingField(db: ISQLiteDatabase) {
        db.execSQL(
            getSqlAddColumn(
                AuthentifiantSql.TABLE_NAME,
                AuthentifiantSql.FIELD_AUTH_LINKED_SERVICES,
                " TEXT DEFAULT '';"
            )
        )
    }
}