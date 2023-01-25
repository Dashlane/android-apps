package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object AddressSql : Sql {
    const val FIELD_NAME = "Name"
    const val FIELD_RECEIVER = "Receiver"
    const val FIELD_FULL = "Full"
    const val FIELD_CITY = "City"
    const val FIELD_ZIPCODE = "Zipcode"
    const val FIELD_STATE = "State"
    const val FIELD_COUNTRY = "Country"
    const val FIELD_STREET_NUMBER = "StreetNumber"
    const val FIELD_STREET_TITLE = "Streettitle"
    const val FIELD_STREET_NAME = "Streetname"
    const val FIELD_STATE_NUMBER = "Statenumber"
    const val FIELD_STATE_LEVEL_2 = "Statelevel2"
    const val FIELD_BUILDING = "Building"
    const val FIELD_STAIRS = "Stairs"
    const val FIELD_FLOOR = "Floor"
    const val FIELD_DOOR = "Door"
    const val FIELD_DIGIT_CODE = "Digitcode"
    const val FIELD_LINKED_PHONE = "Linkedphone"
    const val TABLE_NAME = "Address"
    @JvmField
    val DATABASE_CREATE = ("create table IF NOT EXISTS " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_NAME + " text , " +
            FIELD_RECEIVER + " text , " +
            FIELD_FULL + " text , " +
            FIELD_CITY + " text , " +
            FIELD_ZIPCODE + " text , " +
            FIELD_STATE + " text , " +
            FIELD_COUNTRY + " text , " +
            FIELD_STREET_NUMBER + " text , " +
            FIELD_STREET_TITLE + " text , " +
            FIELD_STREET_NAME + " text , " +
            FIELD_STATE_NUMBER + " text , " +
            FIELD_STATE_LEVEL_2 + " text , " +
            FIELD_BUILDING + " text , " +
            FIELD_STAIRS + " text , " +
            FIELD_FLOOR + " text , " +
            FIELD_DOOR + " text , " +
            FIELD_DIGIT_CODE + " text , " +
            FIELD_LINKED_PHONE + " text );")

    override val tableName: String
        get() = TABLE_NAME
    override val databaseColumns
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(AddressSql.FIELD_NAME).append(",")
            .append(AddressSql.FIELD_RECEIVER).append(",")
            .append(AddressSql.FIELD_FULL).append(",")
            .append(AddressSql.FIELD_CITY).append(",")
            .append(AddressSql.FIELD_ZIPCODE).append(",")
            .append(AddressSql.FIELD_STATE).append(",")
            .append(AddressSql.FIELD_COUNTRY).append(",")
            .append(AddressSql.FIELD_STREET_NUMBER).append(",")
            .append(AddressSql.FIELD_STREET_TITLE).append(",")
            .append(AddressSql.FIELD_STREET_NAME).append(",")
            .append(AddressSql.FIELD_STREET_NUMBER).append(",")
            .append(AddressSql.FIELD_STATE_LEVEL_2).append(",")
            .append(AddressSql.FIELD_BUILDING).append(",")
            .append(AddressSql.FIELD_STAIRS).append(",")
            .append(AddressSql.FIELD_FLOOR).append(",")
            .append(AddressSql.FIELD_DOOR).append(",")
            .append(AddressSql.FIELD_DIGIT_CODE).append(",")
            .append(AddressSql.FIELD_LINKED_PHONE)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
