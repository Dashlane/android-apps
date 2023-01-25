package com.dashlane.database.sql

object SecureNoteCategorySql : Sql {
    const val FIELD_TITLE = "title"
    const val TABLE_NAME = "SecureNoteCategory"
    @JvmField
    val DATABASE_CREATE = ("create table IF NOT EXISTS  " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_TITLE + " text ); ")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns: String
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(SecureNoteCategorySql.FIELD_TITLE)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
