package com.dashlane.storage.userdata.dao;

import android.database.Cursor;

import com.dashlane.storage.userdata.Database;
import com.dashlane.util.CursorUtils;
import com.dashlane.util.JsonSerialization;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;



public abstract class AbstractSharingDao<T> {

    final Database mDatabase;
    final JsonSerialization mJsonSerialization;
    private final Class<T> mClazz;

    public AbstractSharingDao(Class<T> clazz, JsonSerialization jsonSerialization, Database database) {
        mClazz = clazz;
        mJsonSerialization = jsonSerialization;
        mDatabase = database;
    }

    @Nullable
    public T load(String id) {
        Cursor cursor = query(getLabelId() + " = ? ", id);
        T item = loadFirst(cursor);
        CursorUtils.closeCursor(cursor);
        return item;
    }

    @NonNull
    public List<T> loadAll() {
        Cursor cursor = query(null);
        List<T> list = loadAll(cursor);
        CursorUtils.closeCursor(cursor);
        return list;
    }

    public void delete(String id) {
        if (id == null) {
            return;
        }
        mDatabase.executeRawExecSQL(
                "DELETE FROM " + getTableName() + " WHERE " + getLabelId() + " = '" + id + "'");
    }

    public int getCount() {
        return mDatabase.count(getTableName());
    }

    @Nullable
    private T loadFirst(Cursor cursor) {
        if (cursor == null || !cursor.moveToFirst()) {
            return null;
        }
        return deserializeAtPosition(cursor);
    }

    @NonNull
    protected List<T> loadAll(Cursor cursor) {
        List<T> list = new ArrayList<>();
        if (cursor == null || !cursor.moveToFirst()) {
            return list; 
        }
        do {
            T item = deserializeAtPosition(cursor);
            if (item != null) {
                list.add(item);
            }
        } while (cursor.moveToNext());
        return list;
    }

    protected Cursor query(String selection, String... selectionArgs) {
        return mDatabase.query(getTableName(), null, selection, selectionArgs,
                
                
                getSortOrder());
    }

    @Nullable
    private T deserializeAtPosition(@NonNull Cursor cursor) {
        String extraData = CursorUtils.getString(cursor, SharingDataType.ColumnName.EXTRA_DATA);
        try {
            return mJsonSerialization.fromJson(extraData, mClazz);
        } catch (Exception ex) {
            return null;
        }
    }

    

    String getTableName() {
        return getDataType().getTableName();
    }

    

    private String getLabelId() {
        return getDataType().getIdColumnName();
    }

    String getSortOrder() {
        return getLabelId() + " DESC";
    }

    abstract SharingDataType getDataType();

}
