package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object SocialSecurityStatementSql : Sql {
    const val FIELD_SOCIAL_SECURITY_NUMBER = "socialSecurityNumber"
    const val FIELD_SOCIAL_SECURITY_FULLNAME = "socialSecurityFullname"
    const val FIELD_LINKED_IDENTITY = "identity"
    const val FIELD_DATE_OF_BIRTH = "dateofBirth"
    const val FIELD_SEX = "sex"
    const val TABLE_NAME = "SocialSecurityStatement"
    @JvmField
    val DATABASE_CREATE = ("create table  IF NOT EXISTS " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_SOCIAL_SECURITY_FULLNAME + " text , " +
            FIELD_LINKED_IDENTITY + " text , " +
            FIELD_DATE_OF_BIRTH + " text , " +
            FIELD_SEX + " text , " +
            FIELD_SOCIAL_SECURITY_NUMBER + " text ); ")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(SocialSecurityStatementSql.FIELD_SOCIAL_SECURITY_FULLNAME).append(",")
            .append(SocialSecurityStatementSql.FIELD_LINKED_IDENTITY).append(",")
            .append(SocialSecurityStatementSql.FIELD_DATE_OF_BIRTH).append(",")
            .append(SocialSecurityStatementSql.FIELD_SEX).append(",")
            .append(SocialSecurityStatementSql.FIELD_SOCIAL_SECURITY_NUMBER)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
