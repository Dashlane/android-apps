package com.dashlane.db;

import androidx.annotation.VisibleForTesting;

import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.database.sql.AuthentifiantSql;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.storage.tableworker.DatabaseTableWorker;

import net.sqlcipher.SQLException;




class OtpUrlDatabaseTableWorker extends DatabaseTableWorker {
    private static final int OTP_URL_FIELD_SUPPORT_VERSION = 46;

    @Override
    public boolean updateDatabaseTables(ISQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < OTP_URL_FIELD_SUPPORT_VERSION) {
                callUpdateTablesForOtpUrl(db);
            }
            return true;
        } catch (SQLException e) {
            ExceptionLog.v(e);
            return false;
        }
    }

    @Override
    public void createDatabaseTables(ISQLiteDatabase db) {
        
    }

    @VisibleForTesting
    void callUpdateTablesForOtpUrl(ISQLiteDatabase db) {
        
        String table = AuthentifiantSql.TABLE_NAME;
        String column = AuthentifiantSql.FIELD_AUTH_OTP_URL;
        String sql = getSqlAddColumn(table, column, " TEXT DEFAULT '';");
        db.execSQL(sql);
    }

}
