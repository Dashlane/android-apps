package com.dashlane.db

import androidx.annotation.VisibleForTesting
import com.dashlane.database.ISQLiteDatabase
import com.dashlane.database.sql.IdentitySql
import com.dashlane.storage.tableworker.DatabaseTableWorker



class TitleRawTableWorker : DatabaseTableWorker() {

    private val tableName = IdentitySql.TABLE_NAME
    private val fieldTitle = IdentitySql.FIELD_TITLE

    
    
    private val legacyTitleOrdinals = mapOf(
        "MR" to 0,
        "MME" to 1,
        "MLLE" to 2,
        "MS" to 3,
        "" to 4
    )

    override fun updateDatabaseTables(db: ISQLiteDatabase, oldVersion: Int, newVersion: Int): Boolean {
        if (oldVersion < SUPPORT_VERSION) {
            migrateTitleValue(db)
        }
        return true
    }

    private fun migrateTitleValue(db: ISQLiteDatabase) {
        try {
            db.beginTransaction()
            executeUpdates(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun executeUpdates(db: ISQLiteDatabase) {
        getUpdateSQLs().forEach { sql ->
            try {
                db.execSQL(sql)
            } catch (ignored: Exception) {
                
            }
        }
    }

    @VisibleForTesting
    fun getUpdateSQLs(): List<String> {
        return legacyTitleOrdinals.map { (title, ordinal) ->
            "UPDATE $tableName SET $fieldTitle = '$title' WHERE $fieldTitle = '$ordinal'"
        }
    }

    override fun createDatabaseTables(db: ISQLiteDatabase) {
        
    }

    companion object {
        private const val SUPPORT_VERSION = 43
    }
}