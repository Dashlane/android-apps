package com.dashlane.database.sql

object PersonalWebsiteSql : Sql {
    const val FIELD_WEBSITE = "website"
    const val FIELD_NAME = "name"
    const val TABLE_NAME = "PersonalWebsite"
    @JvmField
    val DATABASE_CREATE = ("create table IF NOT EXISTS  " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_NAME + " text not null, " +
            FIELD_WEBSITE + " text not null); ")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(PersonalWebsiteSql.FIELD_NAME).append(",")
            .append(PersonalWebsiteSql.FIELD_WEBSITE)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
