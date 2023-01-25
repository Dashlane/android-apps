package com.dashlane.storage.userdata.internal

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.ChangeSetSql
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.util.getInt
import com.dashlane.util.getLong
import com.dashlane.util.getString
import com.dashlane.util.toBoolean
import com.dashlane.util.toInt
import com.dashlane.xml.SyncObjectEnum
import java.time.Instant

object ChangeSetDbConverter {
    @JvmStatic
    fun getItemFromCursor(c: Cursor): ChangeSetForDb {
        if (c.count <= 0) {
            return ChangeSetForDb(uid = "")
        }
        return ChangeSetForDb(
            sqliteId = c.getLong(DataIdentifierSql.FIELD_ID),
            uid = c.getString(ChangeSetSql.FIELD_UID) ?: "",
            dataChangeHistoryUID = c.getString(ChangeSetSql.FIELD_DATA_CHANGE_HISTORY_UID),
            modificationTimestampSeconds = Instant.ofEpochSecond(c.getLong(ChangeSetSql.FIELD_MODIFICATION_DATE)),
            user = c.getString(ChangeSetSql.FIELD_USER),
            platform = c.getString(ChangeSetSql.FIELD_PLATFORM)?.let { SyncObjectEnum.getEnumForValue(it) },
            deviceName = c.getString(ChangeSetSql.FIELD_DEVICE_NAME),
            isRemoved = c.getInt(ChangeSetSql.FIELD_REMOVED).toBoolean()
        )
    }

    @JvmStatic
    fun getContentValues(item: ChangeSetForDb): ContentValues {
        val cv = ContentValues()
        cv.put(ChangeSetSql.FIELD_UID, item.uid)
        cv.put(ChangeSetSql.FIELD_DATA_CHANGE_HISTORY_UID, item.dataChangeHistoryUID)
        cv.put(ChangeSetSql.FIELD_MODIFICATION_DATE, item.modificationTimestampSeconds?.epochSecond)
        cv.put(ChangeSetSql.FIELD_USER, item.user ?: "")
        cv.put(ChangeSetSql.FIELD_PLATFORM, item.platform?.value)
        cv.put(ChangeSetSql.FIELD_DEVICE_NAME, item.deviceName)
        cv.put(ChangeSetSql.FIELD_REMOVED, item.isRemoved.toInt())
        return cv
    }
}