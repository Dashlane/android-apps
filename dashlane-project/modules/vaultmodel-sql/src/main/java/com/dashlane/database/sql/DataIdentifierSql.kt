package com.dashlane.database.sql

import android.provider.BaseColumns
import com.dashlane.vault.model.SyncState


@Suppress("kotlin:S1192")
object DataIdentifierSql {
    const val FIELD_ID = BaseColumns._ID
    const val FIELD_UID = "uid"
    const val FIELD_ANONYMOUS_UID = "anonymous_uid"
    const val FIELD_EXTRA = "extraData"
    const val FIELD_LOCALE_LANG = "localeLang"
    const val FIELD_ITEM_STATE = "itemState"
    const val FIELD_ITEM_SHARED = "item_shared"
    const val FIELD_HAS_DIRTY_SHARED_FIELD = "has_dirty_shared_field"
    const val FIELD_SHARING_PERMISSION = "sharing_permission"
    const val FIELD_CREATION_DATE = "creation_date"
    const val FIELD_USER_MODIFICATION_DATE = "user_modification_date"
    const val FIELD_LOCALLY_VIEWED_DATE = "locally_viewed_date"
    const val FIELD_LOCALLY_USED_COUNT = "locally_used_count"
    const val FIELD_ATTACHMENTS = "attachments"
    const val FIELD_BACKUP_DATE = "sync_backup_date" 

    

    const val FIELD_USER_DATA_HASH = "user_data_hash"

    @JvmField
    val SHOW_LIST = "(" +
            FIELD_ITEM_STATE + "='" + SyncState.IN_SYNC_MODIFIED.code +
            "' or " +
            FIELD_ITEM_STATE + " ='" + SyncState.MODIFIED.code +
            "' or " +
            FIELD_ITEM_STATE + " ='" + SyncState.SYNCED.code +
            "')"

    @JvmField
    val MAIN_DATABASE_CREATE = (FIELD_ID + " integer primary key autoincrement, " +
            FIELD_UID + " text UNIQUE not null, " +
            FIELD_ANONYMOUS_UID + " text, " +
            FIELD_ITEM_STATE + " text not null default '" + SyncState.SYNCED.code + "' ," +
            FIELD_LOCALE_LANG + " integer, " +
            FIELD_EXTRA + " text, " +
            FIELD_ITEM_SHARED + " INTEGER DEFAULT 0, " +
            FIELD_CREATION_DATE + " text default \'0\', " +
            FIELD_USER_MODIFICATION_DATE + " text default \'0\', " +
            FIELD_LOCALLY_VIEWED_DATE + " text default \'0\', " +
            FIELD_ATTACHMENTS + " text, " +
            FIELD_BACKUP_DATE + " INTEGER DEFAULT 0, " +
            FIELD_USER_DATA_HASH + " INTEGER DEFAULT 0," +
            FIELD_LOCALLY_USED_COUNT + " LONG DEFAULT 0,")

    const val MAIN_DATABASE_COLUMNS = FIELD_ID + "," +
            FIELD_UID + "," +
            FIELD_ITEM_STATE + "," +
            FIELD_LOCALE_LANG + "," +
            FIELD_EXTRA + "," +
            FIELD_ITEM_SHARED + "," +
            FIELD_CREATION_DATE + "," +
            FIELD_USER_MODIFICATION_DATE + "," +
            FIELD_LOCALLY_VIEWED_DATE + " , " +
            FIELD_ATTACHMENTS + "," +
            FIELD_LOCALLY_USED_COUNT + ","

    @JvmStatic
    fun getWhereStatement(uid: String): String = "$FIELD_UID='$uid'"
}