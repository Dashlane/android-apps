package com.dashlane.storage.userdata.internal

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.ChangeSetChangeSql
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.util.getBoolean
import com.dashlane.util.getLong
import com.dashlane.util.getString

object ChangeSetChangeDbConverter {

    @JvmStatic
    fun getItemFromCursor(c: Cursor): ChangeSetChangeForDb {

        if (c.count <= 0) return ChangeSetChangeForDb(uid = "", changeSetUID = "", changedProperty = "")

        return ChangeSetChangeForDb(
            sqliteId = c.getLong(DataIdentifierSql.FIELD_ID),
            uid = c.getString(ChangeSetChangeSql.FIELD_UID)!!,
            changeSetUID = c.getString(ChangeSetChangeSql.FIELD_CHANGESET_UID)!!,
            changedProperty = c.getString(ChangeSetChangeSql.FIELD_CHANGED_PROPERTY)!!,
            currentValue = c.getString(ChangeSetChangeSql.FIELD_CURRENT_VALUE),
            isSavedFromJava = c.getBoolean(ChangeSetChangeSql.FIELD_SAVED_FROM_JAVA)
        )
    }

    @JvmStatic
    fun getContentValues(item: ChangeSetChangeForDb): ContentValues? {
        val cv = ContentValues()
        cv.put(ChangeSetChangeSql.FIELD_UID, item.uid)
        cv.put(ChangeSetChangeSql.FIELD_CHANGESET_UID, item.changeSetUID)
        cv.put(ChangeSetChangeSql.FIELD_CHANGED_PROPERTY, item.changedProperty)
        cv.put(ChangeSetChangeSql.FIELD_CURRENT_VALUE, item.currentValue)
        cv.put(ChangeSetChangeSql.FIELD_SAVED_FROM_JAVA, item.isSavedFromJava)
        return cv
    }
}