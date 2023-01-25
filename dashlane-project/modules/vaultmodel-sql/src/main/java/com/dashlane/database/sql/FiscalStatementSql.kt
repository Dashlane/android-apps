package com.dashlane.database.sql

object FiscalStatementSql : Sql {
    const val FIELD_FISCAL_NUMBER = "fiscal_number"
    const val FIELD_TELEDECLARANT_NUMBER = "teledeclarant_number"
    const val FIELD_LINKED_IDENTITY = "identity"
    const val FIELD_FULLNAME = "fullname"
    const val TABLE_NAME = "FiscalStatement"

    @JvmField
    @SuppressWarnings("kotlin:S1192")
    val DATABASE_CREATE = ("create table  IF NOT EXISTS " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_FISCAL_NUMBER + " text , " +
            FIELD_TELEDECLARANT_NUMBER + " text , " +
            FIELD_LINKED_IDENTITY + " text , " +
            FIELD_FULLNAME + " text); ")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(FiscalStatementSql.FIELD_FISCAL_NUMBER).append(",")
            .append(FiscalStatementSql.FIELD_TELEDECLARANT_NUMBER).append(",")
            .append(FiscalStatementSql.FIELD_LINKED_IDENTITY).append(",")
            .append(FiscalStatementSql.FIELD_FULLNAME)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
