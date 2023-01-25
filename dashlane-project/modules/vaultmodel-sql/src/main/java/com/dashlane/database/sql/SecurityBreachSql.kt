package com.dashlane.database.sql

object SecurityBreachSql : Sql {

    const val FIELD_BREACH_ID = "breachId"
    const val FIELD_CONTENT = "content"
    const val FIELD_CONTENT_REVISION = "contentRevision"
    const val FIELD_STATUS = "status"
    const val FIELD_LEAK_PASSWORDS = "leakPasswords"
    const val TABLE_NAME = "SecurityBreach"

    override val tableName = TABLE_NAME
    @JvmField
    val DATABASE_CREATE = ("create table IF NOT EXISTS $tableName (" +
            DataIdentifierSql.MAIN_DATABASE_CREATE +
            "$FIELD_BREACH_ID text not null, " +
            "$FIELD_CONTENT text not null, " +
            "$FIELD_CONTENT_REVISION INTEGER DEFAULT 0, " +
            "$FIELD_STATUS text not null, " +
            "$FIELD_LEAK_PASSWORDS text);")

    override val createStatement = DATABASE_CREATE

    override val databaseColumns = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
        .append(FIELD_BREACH_ID).append(",")
        .append(FIELD_CONTENT).append(",")
        .append(FIELD_CONTENT_REVISION).append(",")
        .append(FIELD_STATUS).append(",")
        .append(FIELD_LEAK_PASSWORDS)
        .toString()
}