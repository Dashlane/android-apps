package com.dashlane.db;

import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.database.sql.DataIdentifierSql;
import com.dashlane.database.sql.SecurityBreachSql;
import com.dashlane.storage.tableworker.DatabaseTableWorker;
import com.dashlane.vault.model.SyncState;
import com.dashlane.xml.domain.SyncObject;



class SecurityBreachTableWorker extends DatabaseTableWorker {

    SecurityBreachTableWorker() {
    }

    @Override
    public boolean updateDatabaseTables(ISQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 29) {
            
            createDatabaseTables(db);
        } else if (oldVersion < 33) {
            
            updateColumnFromViewedToStatus(db);
        }
        return true;
    }

    @Override
    public void createDatabaseTables(ISQLiteDatabase db) {
        db.execSQL(SecurityBreachSql.DATABASE_CREATE);
    }

    private void updateColumnFromViewedToStatus(ISQLiteDatabase db) {
        db.execSQL(getSqlAddColumn(SecurityBreachSql.TABLE_NAME, SecurityBreachSql.FIELD_STATUS,
                                   "TEXT DEFAULT '" + SyncObject.SecurityBreach.Status.PENDING + "';"));
        db.execSQL("UPDATE " + SecurityBreachSql.TABLE_NAME +
                " SET " + DataIdentifierSql.FIELD_ITEM_STATE + " = '" + SyncState.MODIFIED.getCode() + "'");
        db.execSQL("UPDATE " + SecurityBreachSql.TABLE_NAME +
                   " SET " + SecurityBreachSql.FIELD_STATUS + " = '" + SyncObject.SecurityBreach.Status.VIEWED + "'" +
                   " WHERE viewed = 1");
    }
}
