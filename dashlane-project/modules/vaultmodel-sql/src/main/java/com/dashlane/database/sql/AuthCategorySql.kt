package com.dashlane.database.sql

object AuthCategorySql : Sql {
    const val FIELD_NAME = "name"
    const val TABLE_NAME = "AuthCategory"
    @JvmField
    val DATABASE_CREATE = ("create table IF NOT EXISTS  " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_NAME + " text not null);")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(AuthCategorySql.FIELD_NAME)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
