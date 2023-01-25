package com.dashlane.storage.userdata;

import android.content.Context;

import com.dashlane.database.CipherSQLiteDatabaseWrapper;
import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.useractivity.log.install.InstallLogRepository;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteDatabaseHook;
import net.sqlcipher.database.SQLiteOpenHelper;

import androidx.annotation.VisibleForTesting;

public class CipherDatabase extends Database {

    public CipherDatabase(Context ctx, String username,
                          IDatabaseUpdateManager updateManager,
                          InstallLogRepository installLogRepository) {
        super(ctx, username, updateManager, installLogRepository);
    }

    @Override
    public ISQLiteDatabase openDb(Context context, Secret secret, String name) {
        DatabaseHelper dbHelper = new DatabaseHelper(context, name, secret, mUpdateManager);
        
        SQLiteDatabase db = dbHelper.getWritableDatabase((char[]) null);
        return new CipherSQLiteDatabaseWrapper(db);
    }

    @VisibleForTesting
    static class DatabaseHelper extends SQLiteOpenHelper {
        private IDatabaseUpdateManager mUpdateManager;

        DatabaseHelper(Context context, String dbName, Secret secret, IDatabaseUpdateManager updateManager) {
            super(context, dbName, null, DATABASE_VERSION, new CipherDatabaseHook(secret));
            mUpdateManager = updateManager;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mUpdateManager.createDatabase(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            mUpdateManager.migrateDatabase(db, oldVersion, newVersion);
        }
    }

    static class CipherDatabaseHook implements SQLiteDatabaseHook {

        Secret mSecret;

        public CipherDatabaseHook(Secret secret) {
            mSecret = secret;
        }

        @Override
        public void preKey(SQLiteDatabase sqLiteDatabase) {
        }

        @Override
        public void postKey(SQLiteDatabase database) {
            if (mSecret != null) {
                String[] sqlCommands = mSecret.getSqlCommands();
                if (sqlCommands != null) {
                    for (String sqlCommand : sqlCommands) {
                        database.execSQL(sqlCommand);
                    }
                }
            }
        }
    }

}
