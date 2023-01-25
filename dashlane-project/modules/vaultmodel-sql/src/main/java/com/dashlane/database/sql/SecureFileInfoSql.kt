package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object SecureFileInfoSql : Sql {
    const val FIELD_MIME_TYPE = "mimeType"
    const val FIELD_FILENAME = "filename"
    const val FIELD_DOWNLOAD_KEY = "downloadKey"
    const val FIELD_CRYPTO_KEY = "cryptoKey"
    const val FIELD_REMOTE_SIZE = "remoteSize"
    const val FIELD_LOCAL_SIZE = "localSize"
    const val FIELD_OWNER = "owner"
    const val FIELD_VERSION = "version"
    const val TABLE_NAME = "SecureFileInfo"
    @JvmField
    val DATABASE_CREATE = ("create table IF NOT EXISTS  " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_MIME_TYPE + " text , " +
            FIELD_FILENAME + " text , " +
            FIELD_DOWNLOAD_KEY + " text , " +
            FIELD_CRYPTO_KEY + " text , " +
            FIELD_REMOTE_SIZE + " text , " +
            FIELD_LOCAL_SIZE + " text , " +
            FIELD_OWNER + " text , " +
            FIELD_VERSION + " text ); ")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns: String?
        get() = StringBuilder(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(FIELD_MIME_TYPE).append(",")
            .append(FIELD_FILENAME).append(",")
            .append(FIELD_DOWNLOAD_KEY).append(",")
            .append(FIELD_CRYPTO_KEY).append(",")
            .append(FIELD_REMOTE_SIZE).append(",")
            .append(FIELD_LOCAL_SIZE).append(",")
            .append(FIELD_OWNER).append(",")
            .append(FIELD_VERSION)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
