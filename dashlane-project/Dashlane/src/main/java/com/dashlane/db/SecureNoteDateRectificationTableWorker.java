package com.dashlane.db;

import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.database.sql.DataIdentifierSql;
import com.dashlane.database.sql.SecureNoteSql;
import com.dashlane.storage.tableworker.DatabaseTableWorker;
import com.dashlane.vault.model.SyncState;




public class SecureNoteDateRectificationTableWorker extends DatabaseTableWorker {
    private static final int SECURE_NOTE_RECTIFICATION_DATE = 32;
    private static final String YEAR_2038_TIMESTAMP = Long.toString((long) Integer.MAX_VALUE * 1000);

    @Override
    public boolean updateDatabaseTables(ISQLiteDatabase database, int oldVersion, int newVersion) {
        if (oldVersion < SECURE_NOTE_RECTIFICATION_DATE) {
            rectifySecureNoteColumn(database, DataIdentifierSql.FIELD_CREATION_DATE);
            rectifyYear2038Dates(database, DataIdentifierSql.FIELD_CREATION_DATE);

            rectifySecureNoteColumn(database, DataIdentifierSql.FIELD_USER_MODIFICATION_DATE);
            rectifyYear2038Dates(database, DataIdentifierSql.FIELD_USER_MODIFICATION_DATE);
        }
        return false;
    }

    @Override
    public void createDatabaseTables(ISQLiteDatabase db) {
        
    }

    

    private void rectifySecureNoteColumn(ISQLiteDatabase database, String columnName) {
        String query = "UPDATE " + SecureNoteSql.TABLE_NAME
                + " SET " + columnName + " = " +
                "substr(" + columnName + ", 0, LENGTH(" + columnName + ") - 2) , "
                + DataIdentifierSql.FIELD_ITEM_STATE + " = \"" + SyncState.MODIFIED.getCode() + "\"" +
                " WHERE length(" + columnName + ") >= 15 AND "
                + DataIdentifierSql.FIELD_ITEM_STATE + " NOT IN" +
                "(\"" + SyncState.DELETED.getCode() + "\", \"" + SyncState.IN_SYNC_DELETED.getCode() + "\")";

        database.execSQL(query);
    }

    

    private void rectifyYear2038Dates(ISQLiteDatabase database, String columnName) {
        String query = "UPDATE " + SecureNoteSql.TABLE_NAME
                + " SET " + columnName + " = 0, "
                + DataIdentifierSql.FIELD_ITEM_STATE + " = \"" + SyncState.MODIFIED.getCode() + "\"" +
                " WHERE " + columnName + " == " + YEAR_2038_TIMESTAMP + " AND "
                + DataIdentifierSql.FIELD_ITEM_STATE + " NOT IN" +
                "(\"" + SyncState.DELETED.getCode() + "\", \"" + SyncState.IN_SYNC_DELETED.getCode() + "\")";

        database.execSQL(query);
    }
}