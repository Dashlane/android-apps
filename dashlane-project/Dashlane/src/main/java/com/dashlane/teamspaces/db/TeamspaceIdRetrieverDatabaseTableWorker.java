package com.dashlane.teamspaces.db;

import android.database.Cursor;

import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.database.sql.DataIdentifierSql;
import com.dashlane.database.sql.TeamSpaceSupportingItemSql;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.storage.tableworker.DatabaseTableWorker;
import com.dashlane.util.CursorUtils;
import com.dashlane.vault.model.DataTypeToSql;
import com.dashlane.vault.util.SyncObjectTypeUtils;
import com.dashlane.xml.domain.SyncObjectType;

import net.sqlcipher.SQLException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;



public class TeamspaceIdRetrieverDatabaseTableWorker extends DatabaseTableWorker {

    private static final Pattern EXTRA_DATA_TEAMSPACE_ID_EXTRACTOR = Pattern.compile(
            "<KWDataItem key=\"SpaceId\"><!\\[CDATA\\[([a-zA-Z0-9]*)\\]\\]></KWDataItem>",
            Pattern.DOTALL);

    @Override
    public boolean updateDatabaseTables(ISQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 15) {
                setTeamspaceIdFromExtraData(db);
            }
            return true;
        } catch (SQLException e) {
            ExceptionLog.v(e);
            return false;
        }
    }

    @Override
    public void createDatabaseTables(ISQLiteDatabase db) {
        
    }

    @VisibleForTesting
    void setTeamspaceIdFromExtraData(ISQLiteDatabase db) {
        for (SyncObjectType type : SyncObjectTypeUtils.getWithTeamSpaces()) {
            try {
                updateTeamspaceIdFromExtraData(db, DataTypeToSql.getTableName(type));
            } catch (Exception ex) {
                ExceptionLog.v(ex);
            }
        }
    }

    @VisibleForTesting
    void updateTeamspaceIdFromExtraData(ISQLiteDatabase db, String table) {
        List<String> updateRequests = getUpdateRequestTeamspaceIdFromExtraData(db, table);
        for (String request : updateRequests) {
            db.execSQL(request);
        }
    }

    @VisibleForTesting
    @NonNull
    List<String> getUpdateRequestTeamspaceIdFromExtraData(ISQLiteDatabase db, String table) {
        List<String> updateRequests = new ArrayList<>();
        if (db == null) {
            return updateRequests;
        }
        String sql = "SELECT " + DataIdentifierSql.FIELD_ID + ", " + DataIdentifierSql.FIELD_EXTRA +
                     " FROM " + table +
                     " WHERE (" + DataIdentifierSql.FIELD_EXTRA +
                     " LIKE '%<KWDataItem key=\"SpaceId\"><![CDATA[%' AND " +
                     DataIdentifierSql.FIELD_EXTRA +
                     " NOT LIKE '%<KWDataItem key=\"SpaceId\"><![CDATA[]]></KWDataItem>%')";
        Cursor cursor = db.rawQuery(sql, null);

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndexExtraData = cursor.getColumnIndex(DataIdentifierSql.FIELD_EXTRA);
            int columnIndexId = cursor.getColumnIndex(DataIdentifierSql.FIELD_ID);
            do {
                addUpdateRequestTeamspaceId(updateRequests, table, cursor, columnIndexId, columnIndexExtraData);
            } while (cursor.moveToNext());
        }
        CursorUtils.closeCursor(cursor);
        return updateRequests;
    }

    @VisibleForTesting
    void addUpdateRequestTeamspaceId(List<String> updateRequests, String table, Cursor cursor,
                                     int columnIndexId, int columnIndexExtraData) {
        String extraData = cursor.getString(columnIndexExtraData);
        String spaceId = extractSpaceIdFromExtraData(extraData);
        if (spaceId != null) {
            int id = cursor.getInt(columnIndexId);
            String updateRequest = "UPDATE " + table +
                                   " SET " + TeamSpaceSupportingItemSql.LABEL_TEAMSPACE + "='" + spaceId +
                                   "' WHERE " + DataIdentifierSql.FIELD_ID + " = " + id;
            updateRequests.add(updateRequest);
        }
    }

    @VisibleForTesting
    String extractSpaceIdFromExtraData(String extraData) {
        if (extraData == null) {
            return null;
        }
        Matcher matcher = EXTRA_DATA_TEAMSPACE_ID_EXTRACTOR.matcher(extraData);
        if (matcher.find() && matcher.groupCount() == 1) {
            return matcher.group(1);
        } else {
            return null;
        }
    }
}
