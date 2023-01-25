package com.dashlane.storage.userdata.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.storage.tableworker.DatabaseTableWorker;
import com.dashlane.storage.userdata.Database;
import com.dashlane.util.CursorUtils;
import com.dashlane.util.JsonSerialization;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;



public class SharingItemContentDao extends AbstractSharingDao<ItemContentDB> {

    public SharingItemContentDao(JsonSerialization jsonSerialization,
            Database database) {
        super(ItemContentDB.class, jsonSerialization, database);
    }

    @Override
    SharingDataType getDataType() {
        return SharingDataType.ITEM;
    }

    public String getExtraData(String uid) {
        Cursor cursor = mDatabase.query(getTableName(),
                new String[]{SharingDataType.ColumnName.EXTRA_DATA},
                SharingDataType.ColumnName.ITEM_ID + " = ?",
                new String[]{uid}, getSortOrder());
        String extraData;
        if (cursor != null && cursor.moveToFirst()) {
            extraData = CursorUtils.getString(cursor, SharingDataType.ColumnName.EXTRA_DATA);
        } else {
            extraData = null;
        }
        CursorUtils.closeCursor(cursor);
        return extraData;
    }

    public void saveItemContent(ItemContentDB itemContentDB) {
        save(itemContentDB.getId(),
                itemContentDB.getTimestamp(),
                itemContentDB.getExtraData(),
                itemContentDB.getItemKeyBase64());
    }

    public void save(@NonNull String uid, long timestamp, @Nullable String extraData, @NonNull String itemKeyBase64) {
        ContentValues cv = new ContentValues();
        cv.put(SharingDataType.ColumnName.ITEM_ID, uid);
        cv.put(SharingDataType.ColumnName.ITEM_TIMESTAMP, timestamp);
        cv.put(SharingDataType.ColumnName.EXTRA_DATA, extraData);
        cv.put(SharingDataType.ColumnName.ITEM_KEY, itemKeyBase64);

        mDatabase.insertWithOnConflict(getTableName(), null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    @NonNull
    @Override
    protected List<ItemContentDB> loadAll(Cursor cursor) {
        return ItemContentDBKt.toItemContentDBList(cursor);
    }

    public void update(@NonNull String uid, long timestamp) {
        ContentValues cv = new ContentValues();
        cv.put(SharingDataType.ColumnName.ITEM_TIMESTAMP, timestamp);
        mDatabase.update(getTableName(), cv, SharingDataType.ColumnName.ITEM_ID + " = ?", new String[]{uid});
    }

    public static class TableWorker extends DatabaseTableWorker {

        @Override
        public boolean updateDatabaseTables(ISQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 22) {
                
                createDatabaseTables(db);
            }
            return true;
        }

        @Override
        public void createDatabaseTables(ISQLiteDatabase db) {
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS " + SharingDataType.TableName.ITEM
                    + " ( "
                    + SharingDataType.ColumnName.ITEM_ID + " TEXT PRIMARY KEY NOT NULL, "
                    + SharingDataType.ColumnName.ITEM_TIMESTAMP + " INTEGER, "
                    + SharingDataType.ColumnName.EXTRA_DATA + " TEXT, "
                    + SharingDataType.ColumnName.ITEM_KEY + " TEXT"
                    + ");");
        }
    }

}
