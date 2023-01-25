package com.dashlane.database.sql

object PaymentPaypalSql : Sql {
    const val TABLE_NAME = "PaymentPaypal"
    const val FIELD_NAME = "name"
    const val FIELD_LOGIN = "login"
    const val FIELD_PASSWORD = "password"
    @JvmField
    val DATABASE_CREATE = ("create table  IF NOT EXISTS " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_LOGIN + " text, " +
            FIELD_PASSWORD + " text, " +
            FIELD_NAME + " text); ")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns: String
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(PaymentPaypalSql.FIELD_LOGIN).append(",")
            .append(PaymentPaypalSql.FIELD_PASSWORD).append(",")
            .append(PaymentPaypalSql.FIELD_NAME)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
