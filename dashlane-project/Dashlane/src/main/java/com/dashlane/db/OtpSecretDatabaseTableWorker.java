package com.dashlane.db;

import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.database.sql.AuthentifiantSql;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.storage.tableworker.DatabaseTableWorker;

import net.sqlcipher.SQLException;

import androidx.annotation.VisibleForTesting;




class OtpSecretDatabaseTableWorker extends DatabaseTableWorker {
    private static final int OTP_SECRET_FIELD_SUPPORT_VERSION = 23;

    @Override
    public boolean updateDatabaseTables(ISQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < OTP_SECRET_FIELD_SUPPORT_VERSION) {
                callUpdateTablesForOtpSecret(db);
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
    void callUpdateTablesForOtpSecret(ISQLiteDatabase db) {
        
        String table = AuthentifiantSql.TABLE_NAME;
        String column = AuthentifiantSql.FIELD_AUTH_OTP_SECRET;
        String sql = getSqlAddColumn(table, column, " TEXT DEFAULT '';");
        db.execSQL(sql);
    }

}
