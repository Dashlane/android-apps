package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object IdentitySql : Sql {
    const val FIELD_TYPE = "type"
    const val FIELD_TITLE = "title"
    const val FIELD_FIRSTNAME = "firstname"
    const val FIELD_LASTNAME = "lastname"
    const val FIELD_MIDDLENAME = "middlename"
    const val FIELD_PSEUDO = "pseudo"
    const val FIELD_DATE_OF_BIRTH = "date_of_birth"
    const val FIELD_PLACE_OF_BIRTH = "place_of_birth"
    const val TABLE_NAME = "Identity"

    @JvmField
    val DATABASE_LIST_CREATE = ("create table " + TABLE_NAME + "_linked_cards(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            TABLE_NAME + "_id integer not null, " +
            IdCardSql.TABLE_NAME + "_id integer not null); ")
    @JvmField
    val DATABASE_CREATE = ("create table  IF NOT EXISTS " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_TYPE + " text , " +
            FIELD_TITLE + " text , " +
            FIELD_FIRSTNAME + " text , " +
            FIELD_LASTNAME + " text , " +
            FIELD_MIDDLENAME + " text , " +
            FIELD_PSEUDO + " text , " +
            FIELD_DATE_OF_BIRTH + " text , " +
            FIELD_PLACE_OF_BIRTH + " text ); ")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns: String
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(IdentitySql.FIELD_TYPE).append(",")
            .append(IdentitySql.FIELD_TITLE).append(",")
            .append(IdentitySql.FIELD_FIRSTNAME).append(",")
            .append(IdentitySql.FIELD_LASTNAME).append(",")
            .append(IdentitySql.FIELD_MIDDLENAME).append(",")
            .append(IdentitySql.FIELD_PSEUDO).append(",")
            .append(IdentitySql.FIELD_DATE_OF_BIRTH).append(",")
            .append(IdentitySql.FIELD_PLACE_OF_BIRTH)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
