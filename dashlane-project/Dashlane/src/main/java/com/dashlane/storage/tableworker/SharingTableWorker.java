package com.dashlane.storage.tableworker;

import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.database.sql.AuthentifiantSql;
import com.dashlane.database.sql.DataIdentifierSql;
import com.dashlane.database.sql.SecureNoteSql;



public class SharingTableWorker extends DatabaseTableWorker {

    @Override
    public boolean updateDatabaseTables(ISQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 22) {
            addDirtySharedFieldIntoAuthentifiantAndSecureNote(db);
            addSharingPermissionIntoAuthentifiantAndSecureNote(db);
        }
        return false;
    }

    @Override
    public void createDatabaseTables(ISQLiteDatabase db) {
        
    }

    private void addDirtySharedFieldIntoAuthentifiantAndSecureNote(ISQLiteDatabase db) {
        db.execSQL(getSqlAddColumn(AuthentifiantSql.TABLE_NAME, DataIdentifierSql.FIELD_HAS_DIRTY_SHARED_FIELD,
                                   "INTEGER DEFAULT 0"));
        db.execSQL(getSqlAddColumn(SecureNoteSql.TABLE_NAME, DataIdentifierSql.FIELD_HAS_DIRTY_SHARED_FIELD,
                                   "INTEGER DEFAULT 0"));
    }

    private void addSharingPermissionIntoAuthentifiantAndSecureNote(ISQLiteDatabase db) {
        db.execSQL(getSqlAddColumn(AuthentifiantSql.TABLE_NAME, DataIdentifierSql.FIELD_SHARING_PERMISSION,
                                   "text"));
        db.execSQL(getSqlAddColumn(SecureNoteSql.TABLE_NAME, DataIdentifierSql.FIELD_SHARING_PERMISSION,
                                   "text"));
    }
}
