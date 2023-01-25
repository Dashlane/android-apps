package com.dashlane.database;

import android.content.ContentValues;
import android.database.Cursor;

import net.sqlcipher.database.SQLiteDatabase;

import org.jetbrains.annotations.NotNull;

public class CipherSQLiteDatabaseWrapper implements ISQLiteDatabase {

    private SQLiteDatabase mDatabase;

    public CipherSQLiteDatabaseWrapper(SQLiteDatabase database) {
        mDatabase = database;
    }

    @Override
    public void execSQL(String sql) {
        mDatabase.execSQL(sql);
    }

    @Override
    public void rawExecSQL(String sql) {
        mDatabase.rawExecSQL(sql);
    }

    @Override
    public void beginTransaction() {
        mDatabase.beginTransaction();
    }

    @Override
    public void setTransactionSuccessful() {
        mDatabase.setTransactionSuccessful();
    }

    @Override
    public void endTransaction() {
        mDatabase.endTransaction();
    }

    @Override
    public boolean inTransaction() {
        return mDatabase.inTransaction();
    }

    @Override
    public boolean isOpen() {
        return mDatabase.isOpen();
    }

    @Override
    public int getVersion() {
        return mDatabase.getVersion();
    }

    @Override
    public void close() {
        mDatabase.close();
    }

    @Override
    public long insert(@NotNull String table, String nullColumnHack, ContentValues values) {
        return mDatabase.insert(table, nullColumnHack, values);
    }

    @Override
    public long insertWithOnConflict(@NotNull String table, String nullColumnHack, ContentValues initialValues,
                                     int conflictAlgorithm) {
        return mDatabase.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm);
    }

    @Override
    public int delete(@NotNull String table, String whereClause, String[] whereArgs) {
        return mDatabase.delete(table, whereClause, whereArgs);
    }

    @Override
    public int update(@NotNull String table, ContentValues values, String whereClause, String[] whereArgs) {
        return mDatabase.update(table, values, whereClause, whereArgs);
    }

    @Override
    public Cursor query(@NotNull String table, String[] columns, String selection, String[] selectionArgs, String groupBy,
                        String having, String orderBy, String limit) {
        return mDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    @Override
    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return mDatabase.rawQuery(sql, selectionArgs);
    }

}
