package com.dashlane.database.sql

object EmailSql : Sql {
    const val FIELD_EMAIL_TYPE = "emailType"
    const val FIELD_EMAIL_NAME = "emailName"
    const val FIELD_EMAIL_ADDRESS = "emailAddress"
    const val TABLE_NAME = "Email"
    @JvmField
    val DATABASE_CREATE = ("create table  IF NOT EXISTS " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_EMAIL_TYPE + " text not null, " +
            FIELD_EMAIL_NAME + " text not null, " +
            FIELD_EMAIL_ADDRESS + " text not null);")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(EmailSql.FIELD_EMAIL_TYPE).append(",")
            .append(EmailSql.FIELD_EMAIL_NAME).append(",")
            .append(EmailSql.FIELD_EMAIL_ADDRESS)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
