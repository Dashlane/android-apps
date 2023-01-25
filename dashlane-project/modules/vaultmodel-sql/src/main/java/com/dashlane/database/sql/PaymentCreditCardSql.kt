package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object PaymentCreditCardSql : Sql {
    const val FIELD_CARD_NUMBER = "cardNumber"
    const val FIELD_CARD_NUM_LAST_DIG = "cardNumLastDig"
    const val FIELD_OWNER = "owner"
    const val FIELD_SEC_CODE = "secCode"
    const val FIELD_EXP_MONTH = "expMonth"
    const val FIELD_EXP_YEAR = "expYear"
    const val FIELD_START_MONTH = "startMonth"
    const val FIELD_START_YEAR = "startYear"
    const val FIELD_BILLING_ADDRESS = "billingAddress"
    const val FIELD_COLOR = "color"
    const val FIELD_BANK = "bank"
    const val FIELD_ISSUE_NUM = "issueNumber"
    const val FIELD_NAME = "name"
    const val FIELD_NOTE = "note"
    const val TABLE_NAME = "PaymentCreditCard"
    @JvmField
    val DATABASE_CREATE = ("create table  IF NOT EXISTS " + TABLE_NAME +
            "(" + TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE + FIELD_CARD_NUMBER + " text , " +
            FIELD_CARD_NUM_LAST_DIG + " text , " + FIELD_OWNER + " text , " +
            FIELD_SEC_CODE + " text , " + FIELD_EXP_MONTH + " text , " +
            FIELD_EXP_YEAR + " text , " + FIELD_START_MONTH + " text , " +
            FIELD_START_YEAR + " text , " + FIELD_COLOR + " text , " +
            FIELD_NAME + " text , " + FIELD_BANK + " text , " +
            FIELD_NOTE + " text , " +
            FIELD_ISSUE_NUM + " text , " + FIELD_BILLING_ADDRESS + " text ); ")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns: String
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(PaymentCreditCardSql.FIELD_CARD_NUMBER).append(",")
            .append(PaymentCreditCardSql.FIELD_CARD_NUM_LAST_DIG).append(",")
            .append(PaymentCreditCardSql.FIELD_OWNER).append(",")
            .append(PaymentCreditCardSql.FIELD_SEC_CODE).append(",")
            .append(PaymentCreditCardSql.FIELD_EXP_MONTH).append(",")
            .append(PaymentCreditCardSql.FIELD_EXP_YEAR).append(",")
            .append(PaymentCreditCardSql.FIELD_START_MONTH).append(",")
            .append(PaymentCreditCardSql.FIELD_START_YEAR).append(",")
            .append(PaymentCreditCardSql.FIELD_COLOR).append(",")
            .append(PaymentCreditCardSql.FIELD_NAME).append(",")
            .append(PaymentCreditCardSql.FIELD_NOTE).append(",")
            .append(PaymentCreditCardSql.FIELD_BANK).append(",")
            .append(PaymentCreditCardSql.FIELD_ISSUE_NUM).append(",")
            .append(PaymentCreditCardSql.FIELD_BILLING_ADDRESS)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
