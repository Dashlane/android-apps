package com.dashlane.teamspaces.db;

import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.database.sql.TeamSpaceSupportingItemSql;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.storage.tableworker.DatabaseTableWorker;
import com.dashlane.vault.model.DataTypeToSql;
import com.dashlane.vault.util.SyncObjectTypeUtils;
import com.dashlane.xml.domain.SyncObjectType;

import net.sqlcipher.SQLException;

import androidx.annotation.VisibleForTesting;



public class TeamspaceDatabaseTableWorker extends DatabaseTableWorker {

    @Override
    public boolean updateDatabaseTables(ISQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 14) {
                callUpdateTablesForTeamspace(db);
            }
            if (oldVersion > 14 && oldVersion < 26) {
                callRemoveNullSpaceId(db);
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
    void callRemoveNullSpaceId(ISQLiteDatabase db) {
        for (SyncObjectType type : SyncObjectTypeUtils.getWithTeamSpaces()) {
            updateSpaceIdToRemoveNull(db, DataTypeToSql.getTableName(type));
        }
    }

    @VisibleForTesting
    void callUpdateTablesForTeamspace(ISQLiteDatabase db) {
        for (SyncObjectType type : SyncObjectTypeUtils.getWithTeamSpaces()) {
            addTeamSpaceId(db, DataTypeToSql.getTableName(type));
        }
    }

    @VisibleForTesting
    void addTeamSpaceId(ISQLiteDatabase db, String table) {
        String sql = getSqlAddColumn(table, TeamSpaceSupportingItemSql.LABEL_TEAMSPACE, " TEXT DEFAULT '';");
        db.execSQL(sql);
    }

    private void updateSpaceIdToRemoveNull(ISQLiteDatabase db, String tableName) {
        try {
            String sql = "UPDATE " + tableName + " SET " + TeamSpaceSupportingItemSql.LABEL_TEAMSPACE + " = '' WHERE " +
                         TeamSpaceSupportingItemSql.LABEL_TEAMSPACE + " = 'null'";

            db.execSQL(sql);
        } catch (Exception ignored) {
            
        }
    }


}
