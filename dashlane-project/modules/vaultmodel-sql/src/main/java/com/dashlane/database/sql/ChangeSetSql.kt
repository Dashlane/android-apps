package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object ChangeSetSql : Sql {
    const val TABLE_NAME = "ChangeSet"
    const val FIELD_UID = "changeset_uid"
    const val FIELD_DATA_CHANGE_HISTORY_UID = "changeset_data_change_history_uid"
    const val FIELD_MODIFICATION_DATE = "changeset_modification_date"
    const val FIELD_USER = "changeset_user"
    const val FIELD_PLATFORM = "changeset_platform"
    const val FIELD_DEVICE_NAME = "changeset_device_name"
    const val FIELD_REMOVED = "changeset_removed"
    @JvmField
    val DATABASE_CREATE = ("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            DataIdentifierSql.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            FIELD_UID + " TEXT UNIQUE NOT NULL," +
            FIELD_DATA_CHANGE_HISTORY_UID + " TEXT NOT NULL, " +
            FIELD_MODIFICATION_DATE + " INTEGER, " +
            FIELD_USER + " TEXT NOT NULL, " +
            FIELD_PLATFORM + " TEXT NOT NULL, " +
            FIELD_DEVICE_NAME + " TEXT, " +
            FIELD_REMOVED + " INTEGER, " +
            " FOREIGN KEY (" + FIELD_DATA_CHANGE_HISTORY_UID + ") " +
            " REFERENCES " + DataChangeHistorySql.TABLE_NAME + " (" +
            DataIdentifierSql.FIELD_UID + ")" +
            " ON UPDATE CASCADE" +
            " ON DELETE CASCADE" +
            " );")

    override val tableName: String
        get() = TABLE_NAME
    override val databaseColumns: String? = null
    override val createStatement: String
        get() = DATABASE_CREATE
}
