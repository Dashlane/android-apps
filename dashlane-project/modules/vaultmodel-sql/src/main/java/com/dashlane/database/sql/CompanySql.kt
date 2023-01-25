package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object CompanySql : Sql {
    const val FIELD_NAME = "Name"
    const val FIELD_JOBTITLE = "Jobtitle"
    const val FIELD_SIRET = "Siret"
    const val FIELD_SIREN = "Siren"
    const val FIELD_TVANUMBER = "TvaNumber"
    const val FIELD_NAFCODE = "Nafcode"
    const val TABLE_NAME = "Company"
    @JvmField
    val DATABASE_CREATE = ("create table IF NOT EXISTS  " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_NAME + " text , " +
            FIELD_JOBTITLE + " text , " +
            FIELD_SIRET + " text , " +
            FIELD_SIREN + " text , " +
            FIELD_TVANUMBER + " text , " +
            FIELD_NAFCODE + " text);")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(CompanySql.FIELD_NAME).append(",")
            .append(CompanySql.FIELD_JOBTITLE).append(",")
            .append(CompanySql.FIELD_SIRET).append(",")
            .append(CompanySql.FIELD_SIREN).append(",")
            .append(CompanySql.FIELD_TVANUMBER).append(",")
            .append(CompanySql.FIELD_NAFCODE)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
