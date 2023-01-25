package com.dashlane.storage.tableworker;

import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.database.sql.DataChangeHistorySql;

import androidx.annotation.VisibleForTesting;



public class DataChangeHistoryRevokeTableWorker extends DatabaseTableWorker {

    @Override
    public boolean updateDatabaseTables(ISQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 20) {
            addColumnRevoke(db);
        }
        return true;
    }

    @Override
    public void createDatabaseTables(ISQLiteDatabase db) {
        
    }

    @VisibleForTesting
    void addColumnRevoke(ISQLiteDatabase db) {
        String sqlCommand = getSqlAddColumn(DataChangeHistorySql.TABLE_NAME, DataChangeHistorySql.FIELD_OBJECT_REVOKED,
                                            " INTEGER DEFAULT 0");
        db.execSQL(sqlCommand);
    }

}
