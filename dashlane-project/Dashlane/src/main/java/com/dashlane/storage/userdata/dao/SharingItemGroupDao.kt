package com.dashlane.storage.userdata.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.database.ISQLiteDatabase
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemGroup
import com.dashlane.session.Session
import com.dashlane.sharing.model.getUserStatus
import com.dashlane.storage.tableworker.DatabaseTableWorker
import com.dashlane.storage.userdata.Database
import com.dashlane.util.JsonSerialization



class SharingItemGroupDao(
    jsonSerialization: JsonSerialization,
    database: Database
) : AbstractSharingDao<ItemGroup>(
    ItemGroup::class.java, jsonSerialization, database
) {
    public override fun getDataType(): SharingDataType {
        return SharingDataType.ITEM_GROUP
    }

    fun save(itemGroupServerResponse: ItemGroup) {
        val session: Session = SingletonProvider.getSessionManager().session ?: return
        val myUserId = session.userId
        val cv = ContentValues()
        cv.put(SharingDataType.ColumnName.GROUP_ID, itemGroupServerResponse.groupId)
        cv.put(SharingDataType.ColumnName.GROUP_REVISION, itemGroupServerResponse.revision)
        cv.put(
            SharingDataType.ColumnName.ITEM_GROUP_STATUS,
            itemGroupServerResponse.getUserStatus(myUserId)
        )
        cv.put(
            SharingDataType.ColumnName.EXTRA_DATA,
            mJsonSerialization.toJson(itemGroupServerResponse)
        )
        mDatabase.insertWithOnConflict(tableName, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun loadForItem(itemUID: String): ItemGroup? {
        return loadAll().firstOrNull {
            it.items?.find { it.itemId == itemUID } != null
        }
    }

    class TableWorker : DatabaseTableWorker() {
        override fun updateDatabaseTables(
            db: ISQLiteDatabase,
            oldVersion: Int,
            newVersion: Int
        ): Boolean {
            if (oldVersion < 21) {
                
                createDatabaseTables(db)
            }
            return true
        }

        override fun createDatabaseTables(db: ISQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + SharingDataType.TableName.ITEM_GROUP +
                        " ( " +
                        SharingDataType.ColumnName.GROUP_ID + " TEXT PRIMARY KEY NOT NULL, " +
                        SharingDataType.ColumnName.GROUP_REVISION + " INTEGER, " +
                        SharingDataType.ColumnName.EXTRA_DATA + " TEXT NOT NULL, " +
                        SharingDataType.ColumnName.ITEM_GROUP_STATUS + " TEXT " +
                        ");"
            )
        }
    }
}