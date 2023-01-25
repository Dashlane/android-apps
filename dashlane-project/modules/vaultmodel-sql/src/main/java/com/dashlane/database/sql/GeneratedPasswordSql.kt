package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object GeneratedPasswordSql : Sql {
    const val FIELD_AUTH_DOMAIN = "domain"
    const val FIELD_GENERATED_DATE = "generateddate"
    const val FIELD_PASSWORD = "password"
    const val FIELD_AUTH_ID = "authid"
    const val FIELD_PLATFORM = "platform"
    const val TABLE_NAME = "GeneratedPassword"
    @JvmField
    val DATABASE_CREATE = ("create table  IF NOT EXISTS " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_AUTH_DOMAIN + " text not null, " +
            FIELD_GENERATED_DATE + " text not null, " +
            FIELD_PASSWORD + " text not null, " +
            FIELD_AUTH_ID + " text not null, " +
            FIELD_PLATFORM + " text default null);")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(FIELD_AUTH_DOMAIN).append(",")
            .append(FIELD_GENERATED_DATE).append(",")
            .append(FIELD_PASSWORD).append(",")
            .append(FIELD_AUTH_ID).append(",")
            .append(FIELD_PLATFORM)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
