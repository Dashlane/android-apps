package com.dashlane.storage.tableworker;

import static androidx.annotation.VisibleForTesting.PROTECTED;

import android.database.Cursor;

import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.logger.ExceptionLog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;



public abstract class DatabaseTableWorker {

    

    public abstract boolean updateDatabaseTables(ISQLiteDatabase db, int oldVersion, int newVersion);

    

    public abstract void createDatabaseTables(ISQLiteDatabase db);


    

    protected boolean checkIfColumnExists(@NonNull ISQLiteDatabase database, @NonNull String table,
                                          @NonNull String column) {
        try (Cursor res = database.rawQuery("PRAGMA table_info(" + table + ")", null)) {
            res.moveToFirst();
            do {
                String currentColumn = res.getString(1);
                if (currentColumn.equals(column)) {
                    return true;
                }
            } while (res.moveToNext());
        } catch (Exception e) {
            ExceptionLog.v(e);
        }
        return false;
    }

    

    protected String getSqlAddColumn(String tableName, String columnName) {
        return getSqlAddColumn(tableName, columnName, null);
    }

    

    @VisibleForTesting(otherwise = PROTECTED)
    public String getSqlAddColumn(String tableName, String columnName, @Nullable String additionalInfo) {
        StringBuilder stringBuilder = new StringBuilder()
                .append("ALTER TABLE ")
                .append(tableName)
                .append(" ADD COLUMN ")
                .append(columnName);

        if (additionalInfo != null) {
            stringBuilder.append(" ").append(additionalInfo);
        }
        return stringBuilder.toString();
    }

    

    protected String getSqlCopyColumn(String tableName, String sourceColumnName, String destColumnName) {
        StringBuilder stringBuilder = new StringBuilder()
                .append("UPDATE ")
                .append(tableName)
                .append(" SET ")
                .append(destColumnName)
                .append(" = ")
                .append(sourceColumnName);

        return stringBuilder.toString();
    }

    

    protected String getSqlDropStatement(String typeName, String name) {
        return "DROP " + typeName + " IF EXISTS " +
                name;
    }


}
