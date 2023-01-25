package com.dashlane.storage.userdata.dao;

import android.database.Cursor;

import com.dashlane.storage.userdata.Database;
import com.dashlane.storage.userdata.SqlQuery;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;



public class QueryDao {

    @Inject
    public QueryDao() {
    }

    

    @Deprecated
    @Nullable
    public Cursor getCursorForTable(@Nullable Database database,
                                    @NonNull String tablename,
                                    @Nullable String[] projection,
                                    @Nullable String selection,
                                    @Nullable String[] selectionArgs,
                                    @Nullable String groupBy,
                                    @Nullable String having,
                                    @Nullable String sortOrder) {
        if (database == null) return null;

        SqlQuery sqlQuery = new SqlQuery.Builder(tablename)
                .columns(projection)
                .selection(selection)
                .selectionArgs(selectionArgs)
                .groupBy(groupBy)
                .having(having)
                .orderBy(sortOrder)
                .build();
        return getCursorForTable(database, sqlQuery);
    }

    

    @Nullable
    public Cursor getCursorForTable(@Nullable Database database,
                                    @NonNull SqlQuery sqlQuery) {
        if (database == null) return null;
        try {
            return database.query(sqlQuery);
        } catch (Exception e) {
            return null;
        }
    }

    public Cursor rawQuery(Database database, String query, String[] selectionArgs) {
        if (database != null && database.isOpen()) {
            return database.rawQuery(query, selectionArgs);
        } else {
            return null;
        }
    }
}
