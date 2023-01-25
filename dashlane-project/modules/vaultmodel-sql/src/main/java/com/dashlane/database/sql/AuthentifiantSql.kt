package com.dashlane.database.sql


@Suppress("kotlin:S1192")
object AuthentifiantSql : Sql {

    const val FIELD_TITLE = "title"
    const val FIELD_URL_DEPRECATED = "url"
    const val FIELD_USER_SELECTED_URL = "user_selected_url"
    const val FIELD_USE_FIXED_URL = "use_fixed_url"
    const val FIELD_TRUSTED_URL_GROUP = "trusted_url_group"
    const val FIELD_TRUSTED_URL = "trusted_url"
    const val FIELD_TRUSTED_URL_EXPIRE = "trusted_url_expire"
    const val FIELD_AUTH_EMAIL = "auth_email"
    const val FIELD_AUTH_LOGIN = "auth_login"
    const val FIELD_AUTH_STATUS = "auth_status"
    const val FIELD_AUTH_PASSWORD = "auth_password"
    const val FIELD_AUTH_OTP_SECRET = "auth_otp_secret"
    const val FIELD_AUTH_OTP_URL = "auth_otp_url"
    const val FIELD_AUTH_EXTRA = "auth_extra"
    const val FIELD_AUTH_CATEGORY = "auth_category"
    const val FIELD_AUTH_NOTE = "auth_note"
    const val FIELD_AUTH_AUTOLOGIN = "auth_autologin"
    const val FIELD_AUTH_TEMPORARY_AUTOLOGIN = "auth_temporary_autologin"
    const val FIELD_AUTH_NUMBERUSE = "auth_numberuse"
    const val FIELD_AUTH_LASTUSE = "auth_lastuse"
    const val FIELD_AUTH_STRENGTH = "auth_strength"
    const val FIELD_AUTH_REUSED = "auth_reused"
    const val FIELD_AUTH_META = "auth_meta"

    const val FIELD_CHECKED = "auth_checked"
    const val FIELD_AUTH_PASSWORD_INDEX = "auth_password_index"
    const val TABLE_NAME = "Authentifiant"
    const val FIELD_AUTH_PASSWORD_MODIFICATION_DATE = "auth_password_modification_date"
    const val FIELD_AUTH_LINKED_SERVICES = "auth_linked_services"

    @JvmField
    val DATABASE_CREATE = ("create table IF NOT EXISTS  " + TABLE_NAME + "(" +
            TeamSpaceSupportingItemSql.MAIN_DATABASE_CREATE +
            FIELD_TITLE + " text, " +
            FIELD_URL_DEPRECATED + " text, " +
            FIELD_USER_SELECTED_URL + " text, " +
            FIELD_USE_FIXED_URL + " text, " +
            FIELD_TRUSTED_URL_GROUP + " text, " +
            FIELD_TRUSTED_URL + " text, " +
            FIELD_TRUSTED_URL_EXPIRE + " text, " +
            FIELD_AUTH_EMAIL + " text, " +
            FIELD_AUTH_LOGIN + " text, " +
            FIELD_AUTH_STATUS + " text, " +
            FIELD_AUTH_PASSWORD + " text, " +
            FIELD_AUTH_EXTRA + " text, " +
            FIELD_AUTH_CATEGORY + " text, " +
            FIELD_AUTH_NOTE + " text, " +
            FIELD_AUTH_AUTOLOGIN + " text, " +
            FIELD_AUTH_TEMPORARY_AUTOLOGIN + " text DEFAULT \'false\', " +
            FIELD_AUTH_NUMBERUSE + " text, " +
            FIELD_AUTH_LASTUSE + " text, " +
            FIELD_AUTH_STRENGTH + " text, " +
            FIELD_AUTH_REUSED + " text, " +
            FIELD_AUTH_META + " text, " +
            FIELD_CHECKED + " text default \'false\', " +
            DataIdentifierSql.FIELD_HAS_DIRTY_SHARED_FIELD + " INTEGER DEFAULT 0, " +
            DataIdentifierSql.FIELD_SHARING_PERMISSION + " text, " +
            FIELD_AUTH_PASSWORD_INDEX + " text, " +
            FIELD_AUTH_OTP_SECRET + " text default \'\', " +
            FIELD_AUTH_OTP_URL + " text default \'\', " +
            FIELD_AUTH_PASSWORD_MODIFICATION_DATE + " text DEFAULT \'0\'," +
            FIELD_AUTH_LINKED_SERVICES + " text default \'\');")

    override val tableName: String
        get() = TABLE_NAME

    override val databaseColumns
        get() = StringBuffer(TeamSpaceSupportingItemSql.MAIN_DATABASE_COLUMNS)
            .append(AuthentifiantSql.FIELD_TITLE).append(",")
            .append(AuthentifiantSql.FIELD_URL_DEPRECATED).append(",")
            .append(AuthentifiantSql.FIELD_USER_SELECTED_URL).append(",")
            .append(AuthentifiantSql.FIELD_USE_FIXED_URL).append(",")
            .append(AuthentifiantSql.FIELD_TRUSTED_URL_GROUP).append(",")
            .append(AuthentifiantSql.FIELD_TRUSTED_URL).append(",")
            .append(AuthentifiantSql.FIELD_TRUSTED_URL_EXPIRE).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_EMAIL).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_LOGIN).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_STATUS).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_PASSWORD).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_EXTRA).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_CATEGORY).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_NOTE).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_AUTOLOGIN).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_TEMPORARY_AUTOLOGIN).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_NUMBERUSE).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_LASTUSE).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_STRENGTH).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_REUSED).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_META).append(",")
            .append(AuthentifiantSql.FIELD_CHECKED).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_PASSWORD_INDEX).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_OTP_SECRET).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_OTP_URL).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_PASSWORD_MODIFICATION_DATE).append(",")
            .append(AuthentifiantSql.FIELD_AUTH_LINKED_SERVICES)
            .toString()

    override val createStatement: String
        get() = DATABASE_CREATE
}
