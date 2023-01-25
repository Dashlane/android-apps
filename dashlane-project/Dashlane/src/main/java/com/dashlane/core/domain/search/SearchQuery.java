package com.dashlane.core.domain.search;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.dashlane.vault.util.DataIdentifierSyncObjectTypesKt;
import com.dashlane.vault.util.SyncObjectTypeUtils;
import com.dashlane.xml.domain.SyncObjectType;

import java.time.Instant;

import androidx.annotation.Keep;

@Keep
public class SearchQuery {

    public static final String TABLENAME = "SearchQuery";
    public static final String COLUMN_ID = BaseColumns._ID;
    public static final String COLUMN_DATATYPE = "datatype";
    public static final String COLUMN_DATA_UID = "data_uid";
    public static final String COLUMN_LAST_USED = "last_used";
    public static final String COLUMN_HIT_COUNT = "hit_count";

    private SyncObjectType mQueryResultDataType;
    private String mQueryResultUID;
    private Instant mLastUsed;
    private int mHitCount;

    public SearchQuery() {
        super();
    }

    public SearchQuery(SyncObjectType type, String itemUID) {
        super();
        mQueryResultDataType = type;
        mQueryResultUID = itemUID;
    }

    public static SearchQuery getItemFromCursor(Cursor c) {
        SearchQuery sq = new SearchQuery();
        int indexUid = c.getColumnIndex(COLUMN_DATA_UID);
        int indexType = c.getColumnIndex(COLUMN_DATATYPE);
        int indexLastUsed = c.getColumnIndex(COLUMN_LAST_USED);
        int indexHit = c.getColumnIndex(COLUMN_HIT_COUNT);
        if (indexUid == -1 || indexType == -1 || indexLastUsed == -1 || indexHit == -1) {
            return sq;
        }
        sq.mQueryResultUID = c.getString(indexUid);
        sq.mQueryResultDataType = SyncObjectTypeUtils.valueFromDesktopId(
                c.getInt(indexType));
        sq.mLastUsed = Instant.ofEpochMilli(c.getLong(indexLastUsed));
        sq.mHitCount = c.getInt(indexHit);
        return sq;
    }

    public void setLastUsedToNow() {
        this.mLastUsed = Instant.now();
    }

    public void incrementHitCount() {
        ++mHitCount;
    }

    public SyncObjectType getQueryResultDataType() {
        return mQueryResultDataType;
    }

    public String getQueryResultUID() {
        return mQueryResultUID;
    }

    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_DATA_UID, mQueryResultUID);
        cv.put(COLUMN_DATATYPE, DataIdentifierSyncObjectTypesKt.getDesktopId(mQueryResultDataType));
        cv.put(COLUMN_LAST_USED, mLastUsed.toEpochMilli());
        cv.put(COLUMN_HIT_COUNT, mHitCount);
        return cv;
    }
}

