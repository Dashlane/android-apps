package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object IdCardSql : Sql {
    const val FIELD_FULLNAME = "fullname"
    const val FIELD_SEX = "sex"
    const val FIELD_DATE_OF_BIRTH = "dateofbirth"
    const val FIELD_NUMBER = "number"
    const val FIELD_LINKED_IDENTITY = "identity"
    const val TABLE_NAME = "IdCard"
    @JvmField
    val DATABASE_CREATE = ("create table  IF NOT EXISTS " + TABLE_NAME + "(" +
            TimedDocumentSql.MAIN_DATABASE_CREATE +
            FIELD_FULLNAME + " text , " +
            FIELD_SEX + " text , " +
            FIELD_DATE_OF_BIRTH + " text , " +
            FIELD_LINKED_IDENTITY + " text , " +
            FIELD_NUMBER + " text ); ")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns
        get() = StringBuilder(TimedDocumentSql.MAIN_DATABASE_COLUMNS)
            .append(IdCardSql.FIELD_FULLNAME).append(",")
            .append(IdCardSql.FIELD_SEX).append(",")
            .append(IdCardSql.FIELD_DATE_OF_BIRTH).append(",")
            .append(IdCardSql.FIELD_LINKED_IDENTITY).append(",")
            .append(IdCardSql.FIELD_NUMBER)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
