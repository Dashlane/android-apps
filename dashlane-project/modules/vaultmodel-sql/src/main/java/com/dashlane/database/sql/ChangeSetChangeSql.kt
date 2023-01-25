package com.dashlane.database.sql

object ChangeSetChangeSql : Sql {
    const val TABLE_NAME = "ChangeSetChange"
    const val FIELD_UID = "changeset_change_uid"
    const val FIELD_CHANGESET_UID = "changeset_uid"
    const val FIELD_CHANGED_PROPERTY = "changeset_change_changed_property"
    const val FIELD_CURRENT_VALUE = "changeset_change_current_data"
    const val FIELD_SAVED_FROM_JAVA = "changeset_saved_from_java"
    @JvmField
    val DATABASE_CREATE = ("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            DataIdentifierSql.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            FIELD_UID + " TEXT UNIQUE NOT NULL," +
            FIELD_CHANGESET_UID + " TEXT NOT NULL," +
            FIELD_CHANGED_PROPERTY + " TEXT NOT NULL, " +
            FIELD_CURRENT_VALUE + " TEXT, " +
            FIELD_SAVED_FROM_JAVA + " INTEGER DEFAULT 0," +
            " FOREIGN KEY (" + FIELD_CHANGESET_UID + ") " +
            " REFERENCES " + ChangeSetSql.TABLE_NAME + " (" + ChangeSetSql.FIELD_UID +
            ") ON DELETE CASCADE" +
            " );")

    override val tableName: String
        get() = TABLE_NAME
    override val databaseColumns: String? = null
    override val createStatement: String
        get() = DATABASE_CREATE
}
