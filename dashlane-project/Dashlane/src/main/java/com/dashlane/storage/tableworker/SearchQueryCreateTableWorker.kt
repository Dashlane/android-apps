package com.dashlane.storage.tableworker

import com.dashlane.core.domain.search.SearchQuery.COLUMN_DATATYPE
import com.dashlane.core.domain.search.SearchQuery.COLUMN_DATA_UID
import com.dashlane.core.domain.search.SearchQuery.COLUMN_HIT_COUNT
import com.dashlane.core.domain.search.SearchQuery.COLUMN_ID
import com.dashlane.core.domain.search.SearchQuery.COLUMN_LAST_USED
import com.dashlane.core.domain.search.SearchQuery.TABLENAME
import com.dashlane.database.ISQLiteDatabase

class SearchQueryCreateTableWorker : DatabaseTableWorker() {

    companion object {
        

        const val DATABASE_CREATE = (
                "CREATE TABLE IF NOT EXISTS " + TABLENAME + " ( " +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_DATATYPE + " INTEGER NOT NULL, " +
                        COLUMN_DATA_UID + " TEXT NOT NULL, " +
                        COLUMN_LAST_USED + " INTEGER, " +
                        COLUMN_HIT_COUNT + " INTEGER DEFAULT 0, " +
                        " UNIQUE ( " +
                        COLUMN_DATATYPE + ", " +
                        COLUMN_DATA_UID +
                        " ) " +
                        " ); ")
    }

    override fun updateDatabaseTables(db: ISQLiteDatabase?, oldVersion: Int, newVersion: Int): Boolean {
        

        if (oldVersion < 38) {
            db?.execSQL(DATABASE_CREATE)
        }
        return true
    }

    override fun createDatabaseTables(db: ISQLiteDatabase?) {
        db?.execSQL(DATABASE_CREATE)
    }
}