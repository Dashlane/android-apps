package com.dashlane.storage.userdata.dao

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.dashlane.database.ISQLiteDatabase
import com.dashlane.server.api.endpoints.sharinguserdevice.UserGroup
import com.dashlane.sharing.model.getUser
import com.dashlane.sharing.model.isAccepted
import com.dashlane.sharing.model.isAcceptedOrPending
import com.dashlane.storage.tableworker.DatabaseTableWorker
import com.dashlane.storage.userdata.Database
import com.dashlane.util.JsonSerialization



class SharingUserGroupDao(jsonSerialization: JsonSerialization, database: Database) :
    AbstractSharingDao<UserGroup>(
        UserGroup::class.java, jsonSerialization, database
    ) {
    public override fun getDataType(): SharingDataType {
        return SharingDataType.USER_GROUP
    }

    fun save(userGroupDownloadServerResponse: UserGroup) {
        val cv = ContentValues()
        cv.put(
            SharingDataType.ColumnName.GROUP_ID,
            userGroupDownloadServerResponse.groupId
        )
        cv.put(
            SharingDataType.ColumnName.GROUP_REVISION,
            userGroupDownloadServerResponse.revision
        )
        cv.put(
            SharingDataType.ColumnName.EXTRA_DATA,
            mJsonSerialization.toJson(userGroupDownloadServerResponse)
        )
        mDatabase.insertWithOnConflict(tableName, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun loadUserGroupsAcceptedOrPending(userId: String): List<UserGroup> {
        return loadAll().filter { it.getUser(userId)?.isAcceptedOrPending == true }
    }

    fun loadUserGroupsAccepted(userId: String): List<UserGroup> {
        return loadAll().filter { it.getUser(userId)?.isAccepted == true }
    }

    class TableWorker : DatabaseTableWorker() {
        override fun updateDatabaseTables(
            db: ISQLiteDatabase,
            oldVersion: Int,
            newVersion: Int
        ): Boolean {
            if (oldVersion < 24) {
                
                createDatabaseTables(db)
            }
            return true
        }

        override fun createDatabaseTables(db: ISQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS " + SharingDataType.TableName.USER_GROUP +
                        " ( " +
                        SharingDataType.ColumnName.GROUP_ID + " TEXT PRIMARY KEY NOT NULL, " +
                        SharingDataType.ColumnName.GROUP_REVISION + " INTEGER, " +
                        SharingDataType.ColumnName.EXTRA_DATA + " TEXT NOT NULL" +
                        ");"
            )
        }
    }
}