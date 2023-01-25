package com.dashlane.storage.userdata.dao

import com.dashlane.database.sql.DataIdentifierSql



enum class SharingDataType(
    val tableName: String,
    val idColumnName: String,
    val revisionColumnName: String
) {
    ITEM(TableName.ITEM, ColumnName.ITEM_ID, ColumnName.ITEM_TIMESTAMP),
    ITEM_GROUP(TableName.ITEM_GROUP, ColumnName.GROUP_ID, ColumnName.GROUP_REVISION),
    USER_GROUP(TableName.USER_GROUP, ColumnName.GROUP_ID, ColumnName.GROUP_REVISION);

    object TableName {
        const val ITEM = "SharedItemContent"
        const val ITEM_GROUP = "SharedItemGroup"
        const val USER_GROUP = "SharedUserGroup"
    }

    object ColumnName {
        const val ITEM_ID = DataIdentifierSql.FIELD_UID
        const val ITEM_TIMESTAMP = "remote_timestamp"
        const val ITEM_KEY = "item_key"
        const val GROUP_ID = "groupId"
        const val GROUP_REVISION = "revision"
        const val EXTRA_DATA = "extraData"
        const val ITEM_GROUP_STATUS = "myStatus"
    }
}