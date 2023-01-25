package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object BankStatementSql : Sql {
    const val FIELD_NAME = "name"
    const val FIELD_OWNER = "owner"
    const val FIELD_BIC = "bic"
    const val FIELD_IBAN = "iban"
    const val FIELD_BANK = "bank"
    const val TABLE_NAME = "BankStatement"
    @JvmField
    val DATABASE_CREATE = ("create table IF NOT EXISTS  " + TABLE_NAME +
            "(" + TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_NAME + " text , " +
            FIELD_OWNER + " text , " +
            FIELD_BIC + " text , " +
            FIELD_BANK + " text , " +
            FIELD_IBAN + " text ); ")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns: String
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(BankStatementSql.FIELD_NAME).append(",")
            .append(BankStatementSql.FIELD_OWNER).append(",")
            .append(BankStatementSql.FIELD_BIC).append(",")
            .append(BankStatementSql.FIELD_BANK).append(",")
            .append(BankStatementSql.FIELD_IBAN)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
