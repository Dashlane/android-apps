package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object PhoneSql : Sql {
    const val FIELD_TYPE = "type"
    const val FIELD_PHONE_NUMBER = "number"
    const val FIELD_PHONE_NUMBER_NATIONAL = "number_national"
    const val FIELD_PHONE_NUMBER_INTERNATIONAL = "number_international"
    const val FIELD_PHONE_NAME = "name"
    const val TABLE_NAME = "Phone"
    @JvmField
    val DATABASE_CREATE = ("create table IF NOT EXISTS  " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_TYPE + " text , " +
            FIELD_PHONE_NUMBER + " text , " +
            FIELD_PHONE_NUMBER_NATIONAL + " text , " +
            FIELD_PHONE_NUMBER_INTERNATIONAL + " text , " +
            FIELD_PHONE_NAME + " text ); ")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns: String
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(PhoneSql.FIELD_TYPE).append(",")
            .append(PhoneSql.FIELD_PHONE_NUMBER).append(",")
            .append(PhoneSql.FIELD_PHONE_NUMBER_NATIONAL).append(",")
            .append(PhoneSql.FIELD_PHONE_NUMBER_INTERNATIONAL).append(",")
            .append(PhoneSql.FIELD_PHONE_NAME)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
