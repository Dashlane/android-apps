package com.dashlane.database.sql

object DataChangeHistorySql : Sql {
    const val TABLE_NAME = "DataChangeHistory"
    const val FIELD_OBJECT_UID = "datachange_history_object_uid"
    const val FIELD_OBJECT_TYPE = "datachange_history_object_type"
    const val FIELD_OBJECT_TITLE = "datachange_history_title"
    const val FIELD_OBJECT_REVOKED = "datachange_history_object_revoked"
    @JvmField
    val DATABASE_CREATE = ("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            DataIdentifierSql.MAIN_DATABASE_CREATE +
            FIELD_OBJECT_UID + " TEXT NOT NULL, " +
            FIELD_OBJECT_TYPE + " INTEGER, " +
            FIELD_OBJECT_REVOKED + " INTEGER, " +
            FIELD_OBJECT_TITLE + " TEXT, " +
            " UNIQUE ( " + FIELD_OBJECT_UID + ", " + FIELD_OBJECT_TYPE + " ) " +
            " );")
    @JvmField
    val VIEW_HISTORY_JOIN_ALL = "VIEW_HISTORY_JOIN_ALL"

    override val tableName: String
        get() = TABLE_NAME
    override val databaseColumns: String? get() = null
    override val createStatement: String
        get() = DATABASE_CREATE
}
