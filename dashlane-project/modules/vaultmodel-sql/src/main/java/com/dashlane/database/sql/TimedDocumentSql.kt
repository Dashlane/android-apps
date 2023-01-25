package com.dashlane.database.sql

object TimedDocumentSql {
    const val FIELD_DELIVERY_DATE = "deliverydate"
    const val FIELD_EXPIERY_DATE = "expierydate"

    @JvmField
    val MAIN_DATABASE_CREATE = (TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_DELIVERY_DATE + " text , " +
            FIELD_EXPIERY_DATE + " text , ")

    @JvmField
    val MAIN_DATABASE_COLUMNS = StringBuilder()
        .append(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
        .append(FIELD_DELIVERY_DATE).append(",")
        .append(FIELD_EXPIERY_DATE).append(",").toString()
}
