package com.dashlane.storage.userdata.dao

enum class SharingDataType(
    val tableName: String
) {
    ITEM(TableName.ITEM),
    ITEM_GROUP(TableName.ITEM_GROUP),
    USER_GROUP(TableName.USER_GROUP);

    object TableName {
        const val ITEM = "SharedItemContent"
        const val ITEM_GROUP = "SharedItemGroup"
        const val USER_GROUP = "SharedUserGroup"
    }
}