package com.dashlane.database.sql

object TimedDocumentSql {
    const val FIELD_DELIVERY_DATE = "deliverydate"
    const val FIELD_EXPIRY_DATE = "expirydate"

    @JvmField
    val MAIN_DATABASE_CREATE = (TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_DELIVERY_DATE + " text , " +
            FIELD_EXPIRY_DATE + " text , ")

    @JvmField
    val MAIN_DATABASE_COLUMNS = StringBuilder()
        .append(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
        .append(FIELD_DELIVERY_DATE).append(",")
        .append(FIELD_EXPIRY_DATE).append(",").toString()
}
