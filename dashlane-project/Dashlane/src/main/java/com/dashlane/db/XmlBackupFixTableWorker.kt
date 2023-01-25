package com.dashlane.db

import android.content.ContentValues
import com.dashlane.database.ISQLiteDatabase
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.storage.tableworker.DatabaseTableWorker
import com.dashlane.util.toList
import com.dashlane.vault.model.toSql
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.serializer.XmlSerialization



class XmlBackupFixTableWorker : DatabaseTableWorker() {

    private val xmlSerialization: XmlSerialization = XmlSerialization

    override fun updateDatabaseTables(db: ISQLiteDatabase, oldVersion: Int, newVersion: Int): Boolean {
        if (oldVersion < XML_BACKUP_FIX_VERSION) {
            fixXmlBackup(db)
        }

        return true
    }

    private fun fixXmlBackup(db: ISQLiteDatabase) {
        val dataIdentifierTables = enumValues<SyncObjectType>().mapNotNull { it.toSql()?.tableName }
        for (table in dataIdentifierTables) {
            fixXmlBackup(db, table)
        }
    }

    private fun fixXmlBackup(db: ISQLiteDatabase, tableName: String) {
        val xmlBackupList = db.query(
            tableName,
            arrayOf(DataIdentifierSql.FIELD_ID, DataIdentifierSql.FIELD_EXTRA),
            DataIdentifierSql.FIELD_EXTRA + " IS NOT NULL AND " +
                    DataIdentifierSql.FIELD_EXTRA + " NOT LIKE '%<root>%'",
            null,
            null,
            null,
            null,
            null
        )!!.use {
            val idColumnIndex = it.getColumnIndex(DataIdentifierSql.FIELD_ID)
            val backupColumnIndex = it.getColumnIndex(DataIdentifierSql.FIELD_EXTRA)
            it.toList {
                XmlBackup(getLong(idColumnIndex), getString(backupColumnIndex))
            }
        }
        for (xmlBackup in xmlBackupList) {
            fixXmlBackup(db, tableName, xmlBackup)
        }
    }

    private fun fixXmlBackup(
        db: ISQLiteDatabase,
        tableName: String,
        xmlBackup: XmlBackup
    ) {
        
        runCatching { xmlSerialization.deserializeTransaction(xmlBackup.xml) }
            .onSuccess {
                db.update(
                    tableName,
                    ContentValues().apply {
                        put(DataIdentifierSql.FIELD_EXTRA, xmlSerialization.serializeTransaction(it))
                    },
                    DataIdentifierSql.FIELD_ID + " = ?",
                    arrayOf(xmlBackup.id.toString())
                )
            }
    }

    override fun createDatabaseTables(db: ISQLiteDatabase) {
        
    }

    private data class XmlBackup(
        val id: Long,
        val xml: String
    )

    companion object {
        private const val XML_BACKUP_FIX_VERSION = 39
    }
}