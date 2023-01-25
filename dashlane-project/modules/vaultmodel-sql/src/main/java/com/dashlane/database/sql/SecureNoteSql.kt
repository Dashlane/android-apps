package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object SecureNoteSql : Sql {
    const val FIELD_TYPE = "type"
    const val FIELD_CATEGORY = "category"
    const val FIELD_TITLE = "title"
    const val FIELD_CONTENT = "content"
    const val FIELD_SECURED = "secured"
    const val TABLE_NAME = "SecureNote"
    @JvmField
    val DATABASE_CREATE = ("create table IF NOT EXISTS  " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_TYPE + " text , " +
            FIELD_TITLE + " text , " +
            FIELD_CATEGORY + " text , " +
            FIELD_CONTENT + " text , " +
            DataIdentifierSql.FIELD_HAS_DIRTY_SHARED_FIELD + " INTEGER DEFAULT 0, " +
            DataIdentifierSql.FIELD_SHARING_PERMISSION + " text, " +
            FIELD_SECURED + " text ); ")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns: String
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(SecureNoteSql.FIELD_TYPE).append(",")
            .append(SecureNoteSql.FIELD_TITLE).append(",")
            .append(SecureNoteSql.FIELD_CATEGORY).append(",")
            .append(SecureNoteSql.FIELD_CONTENT).append(",")
            .append(SecureNoteSql.FIELD_SECURED)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
