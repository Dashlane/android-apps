package com.dashlane.storage.tableworker

import com.dashlane.database.ISQLiteDatabase
import com.dashlane.database.sql.FiscalStatementSql

class FiscalStatementFieldsTableWorker : DatabaseTableWorker() {
    override fun updateDatabaseTables(db: ISQLiteDatabase, oldVersion: Int, newVersion: Int): Boolean {
        if (oldVersion < 45) {
            addMissingFields(db)
        }
        return true
    }

    override fun createDatabaseTables(db: ISQLiteDatabase?) {
        
    }

    fun addMissingFields(db: ISQLiteDatabase) {
        db.execSQL(getSqlAddColumn(FiscalStatementSql.TABLE_NAME, FiscalStatementSql.FIELD_FULLNAME, "text"))
        db.execSQL(getSqlAddColumn(FiscalStatementSql.TABLE_NAME, FiscalStatementSql.FIELD_LINKED_IDENTITY, "text"))
    }
}