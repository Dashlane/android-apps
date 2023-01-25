package com.dashlane.teamspaces.db;

import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.storage.tableworker.DatabaseTableWorker;

import net.sqlcipher.SQLException;



public class TeamspaceRemovalTableWorker extends DatabaseTableWorker {

    @Override
    public boolean updateDatabaseTables(ISQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion >= 14 && oldVersion < 16) {
                
                removeTableTeamspace(db);
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

    private void removeTableTeamspace(ISQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS ts_teamspace");
    }

}
