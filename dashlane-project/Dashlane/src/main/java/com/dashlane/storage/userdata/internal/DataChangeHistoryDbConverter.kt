package com.dashlane.storage.userdata.internal

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.converter.DataIdentifierDbConverter
import com.dashlane.database.sql.DataChangeHistorySql
import com.dashlane.util.getInt
import com.dashlane.util.getString
import com.dashlane.xml.domain.SyncObjectType

object DataChangeHistoryDbConverter {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    fun cursorToItem(c: Cursor): DataChangeHistoryForDb {
        return DataChangeHistoryForDb(
            DataIdentifierDbConverter.loadDataIdentifier(c, SyncObjectType.DATA_CHANGE_HISTORY),
            objectUID = c.getString(DataChangeHistorySql.FIELD_OBJECT_UID) ?: "",
            objectTypeId = c.getInt(DataChangeHistorySql.FIELD_OBJECT_TYPE),
            objectTitle = c.getString(DataChangeHistorySql.FIELD_OBJECT_TITLE)
        )
    }

    @JvmStatic
    fun getContentValues(item: DataChangeHistoryForDb) = toContentValues(item)

    fun toContentValues(item: DataChangeHistoryForDb): ContentValues? {
        val cv = DataIdentifierDbConverter.toContentValues(item.dataIdentifier)
        cv.put(DataChangeHistorySql.FIELD_OBJECT_UID, item.objectUID)
        cv.put(DataChangeHistorySql.FIELD_OBJECT_TYPE, item.objectTypeId)
        cv.put(DataChangeHistorySql.FIELD_OBJECT_TITLE, item.objectTitle)
        return cv
    }
}
