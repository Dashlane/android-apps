package com.dashlane.db

import com.dashlane.database.ISQLiteDatabase
import com.dashlane.database.sql.GeneratedPasswordSql
import com.dashlane.storage.tableworker.DatabaseTableWorker



class PasswordGeneratorPlatformTableWorker : DatabaseTableWorker() {

    override fun updateDatabaseTables(db: ISQLiteDatabase, oldVersion: Int, newVersion: Int): Boolean {
        if (oldVersion < SUPPORT_VERSION) {
            addGeneratedPasswordPlatform(db)
        }
        return true
    }

    private fun addGeneratedPasswordPlatform(db: ISQLiteDatabase) {
        if (!checkIfColumnExists(db, GeneratedPasswordSql.TABLE_NAME, GeneratedPasswordSql.FIELD_PLATFORM)) {
            try {
                db.beginTransaction()
                addGeneratedPasswordPlatformColumn(db)
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }

    private fun addGeneratedPasswordPlatformColumn(db: ISQLiteDatabase) {
        val sqlAddColumn =
            getSqlAddColumn(GeneratedPasswordSql.TABLE_NAME, GeneratedPasswordSql.FIELD_PLATFORM, "text default null;")
        db.execSQL(sqlAddColumn)
    }

    override fun createDatabaseTables(db: ISQLiteDatabase) {
        
    }

    companion object {
        private const val SUPPORT_VERSION = 44
    }
}