package com.dashlane.db

import android.content.ContentValues
import com.dashlane.database.ISQLiteDatabase
import com.dashlane.database.converter.DbConverter
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.storage.tableworker.DatabaseTableWorker
import com.dashlane.storage.userdata.asSequence
import com.dashlane.vault.model.toSql
import com.dashlane.xml.domain.SyncObjectType



class UserDataHashTableWorker : DatabaseTableWorker() {

    override fun updateDatabaseTables(db: ISQLiteDatabase, oldVersion: Int, newVersion: Int): Boolean {
        if (oldVersion < USER_DATA_HASH_SUPPORT_VERSION) {
            addUserDataHashIfNotExists(db)
        }

        return true
    }

    private fun addUserDataHashIfNotExists(db: ISQLiteDatabase) {
        val dataIdentifierTables = enumValues<SyncObjectType>().mapNotNull { it.toSql()?.tableName?.to(it) }
        for ((table, type) in dataIdentifierTables) {
            if (!checkIfColumnExists(db, table, DataIdentifierSql.FIELD_USER_DATA_HASH)) {
                try {
                    db.beginTransaction()
                    addUserDataHashColumn(db, table)
                    addUserDataHashes(db, table, type)
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }
    }

    private fun addUserDataHashColumn(db: ISQLiteDatabase, tableName: String) {
        val sql = getSqlAddColumn(
            tableName,
            DataIdentifierSql.FIELD_USER_DATA_HASH,
            "INTEGER DEFAULT 0;"
        )
        db.execSQL(sql)
    }

    private fun addUserDataHashes(
        db: ISQLiteDatabase,
        tableName: String,
        type: SyncObjectType
    ) {
        if (!type.hasDeduplication) return
        db.query(tableName)!!.use { cursor ->
            cursor.asSequence()
                .mapNotNull { DbConverter.fromCursor(it, type) }
                .forEach {
                    db.update(
                        tableName,
                        ContentValues().apply {
                            put(DataIdentifierSql.FIELD_USER_DATA_HASH, it.syncObject.userContentData.hashCode())
                        },
                        "${DataIdentifierSql.FIELD_ID} = ?",
                        arrayOf(it.id.toString())
                    )
                }
        }
    }

    override fun createDatabaseTables(db: ISQLiteDatabase) {
        
    }

    companion object {
        private const val USER_DATA_HASH_SUPPORT_VERSION = 41
    }
}