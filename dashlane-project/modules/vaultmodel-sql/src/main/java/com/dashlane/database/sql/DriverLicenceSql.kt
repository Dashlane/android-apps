package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object DriverLicenceSql : Sql {
    const val FIELD_DATE_OF_BIRTH = "dateofbirth"
    const val FIELD_FULLNAME = "fullname"
    const val FIELD_NUMBER = "number"
    const val FIELD_SEX = "sex"
    const val FIELD_STATE = "state"
    const val FIELD_LINKED_IDENTITY = "identity"
    const val TABLE_NAME = "DriverLicence"
    @JvmField
    val DATABASE_CREATE = ("create table  IF NOT EXISTS " + TABLE_NAME + "(" +
            TimedDocumentSql.MAIN_DATABASE_CREATE +
            FIELD_DATE_OF_BIRTH + " text , " +
            FIELD_FULLNAME + " text , " +
            FIELD_NUMBER + " text , " +
            FIELD_SEX + " text , " +
            FIELD_LINKED_IDENTITY + " text , " +
            FIELD_STATE + " text ); ")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns
        get() = StringBuilder(TimedDocumentSql.MAIN_DATABASE_COLUMNS)
            .append(DriverLicenceSql.FIELD_DATE_OF_BIRTH).append(",")
            .append(DriverLicenceSql.FIELD_FULLNAME).append(",")
            .append(DriverLicenceSql.FIELD_NUMBER).append(",")
            .append(DriverLicenceSql.FIELD_SEX).append(",")
            .append(DriverLicenceSql.FIELD_LINKED_IDENTITY).append(",")
            .append(DriverLicenceSql.FIELD_STATE)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
