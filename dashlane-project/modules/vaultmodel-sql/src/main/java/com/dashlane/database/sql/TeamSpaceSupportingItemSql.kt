package com.dashlane.database.sql

object TeamSpaceSupportingItemSql {
    @JvmField
    val LABEL_TEAMSPACE = "teamspace_id"

    @JvmField
    val MAIN_DATABASE_COLUMNS = DataIdentifierSql.MAIN_DATABASE_COLUMNS +
            LABEL_TEAMSPACE + ", "

    @JvmField
    val MAIN_DATABASE_CREATE = DataIdentifierSql.MAIN_DATABASE_CREATE +
            LABEL_TEAMSPACE + " text,"
}
