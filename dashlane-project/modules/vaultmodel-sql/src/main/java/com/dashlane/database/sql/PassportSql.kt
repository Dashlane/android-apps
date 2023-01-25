package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object PassportSql : Sql {
    const val FIELD_NUMBER = "number"
    const val FIELD_FULLNAME = "fullname"
    const val FIELD_SEX = "sex"
    const val FIELD_DATE_OF_BIRTH = "dateofbirth"
    const val FIELD_DELIVERY_PLACE = "delivery_place"
    const val FIELD_LINKED_IDENTITY = "identity" 
    const val TABLE_NAME = "Passport"
    @JvmField
    val DATABASE_CREATE = ("create table IF NOT EXISTS " + TABLE_NAME + "(" +
            TimedDocumentSql.MAIN_DATABASE_CREATE +
            FIELD_NUMBER + " text , " +
            FIELD_FULLNAME + " text , " +
            FIELD_SEX + " text , " +
            FIELD_LINKED_IDENTITY + " text , " +
            FIELD_DATE_OF_BIRTH + " text , " +
            FIELD_DELIVERY_PLACE + " text ); ")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns: String
        get() = StringBuilder(TimedDocumentSql.MAIN_DATABASE_COLUMNS)
            .append(PassportSql.FIELD_NUMBER).append(",")
            .append(PassportSql.FIELD_FULLNAME).append(",")
            .append(PassportSql.FIELD_SEX).append(",")
            .append(PassportSql.FIELD_LINKED_IDENTITY).append(",")
            .append(PassportSql.FIELD_DATE_OF_BIRTH).append(",")
            .append(PassportSql.FIELD_DELIVERY_PLACE)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
