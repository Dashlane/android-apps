package com.dashlane.storage.tableworker

import com.dashlane.database.ISQLiteDatabase

class EmergencySharingV1DropTableWorker : DatabaseTableWorker() {

    override fun createDatabaseTables(db: ISQLiteDatabase?) {
        
    }

    override fun updateDatabaseTables(db: ISQLiteDatabase, oldVersion: Int, newVersion: Int): Boolean {
        if (oldVersion < EMERGENCY_DROP_DB_VERSION) {
            dropDeprecatedTable(db)
        }
        if (oldVersion < TRIGGER_VIEW_DROP_DB_VERSION) {
            dropSharingTriggerAndView(db)
        }
        return true
    }

    private fun dropDeprecatedTable(database: ISQLiteDatabase) {
        IMPACTED_TABLES_LIST.forEach {
            val sql = getSqlDropStatement("TABLE", it)
            database.execSQL(sql)
        }
    }

    private fun dropSharingTriggerAndView(database: ISQLiteDatabase) {
        SHARING_VIEW_TABLES_LIST.forEach {
            val sql = getSqlDropStatement("VIEW", it)
            database.execSQL(sql)
        }
        SHARING_TRIGGER_TABLES_LIST.forEach {
            val sql = getSqlDropStatement("TRIGGER", it)
            database.execSQL(sql)
        }
    }

    companion object {
        const val EMERGENCY_DROP_DB_VERSION = 34
        const val TRIGGER_VIEW_DROP_DB_VERSION = 35

        val IMPACTED_TABLES_LIST =
            arrayOf(
                "EmergencyBundleItem",
                "EmergencyBundle",
                "EmergencyBundleItemContent",
                "RemoteSharingTransaction",
                "SharingGroupMember",
                "SharingGroup",
                "SharingKey",
                "SharingMember",
                "SharingPublicKeyCache"
            )

        val SHARING_TRIGGER_TABLES_LIST =
            arrayOf(
                "AUTHENTIFIANT_ADDED_CHECK_SHARED",
                "AUTHENTIFIANT_TRIGGER_ON_SHARED_ITEM_TABLE",
                "SECURENOTE_ADDED_CHECK_SHARED",
                "SECURENOTE_TRIGGER_ON_SHARED_ITEM_TABLE",
                "TRIGGER_ON_GROUP_STATE_CHANGE"
            )

        val SHARING_VIEW_TABLES_LIST =
            arrayOf(
                "VIEW_TO_SHARING_GROUP_JOIN_MEMBER",
                "VIEW_PENDING_INVITES",
                "VIEW_EMERGENCY_ITEM_SELECTION"
            )
    }
}