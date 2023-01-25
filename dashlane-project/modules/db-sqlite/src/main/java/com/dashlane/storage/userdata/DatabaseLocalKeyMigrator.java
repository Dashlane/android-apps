package com.dashlane.storage.userdata;

import android.content.Context;

import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.session.AppKey;
import com.dashlane.session.LocalKey;
import com.dashlane.session.LocalKeyKt;
import com.dashlane.useractivity.log.install.InstallLogRepository;

import net.sqlcipher.SQLException;

import java.io.File;
import java.lang.ref.WeakReference;

class DatabaseLocalKeyMigrator {

    private static final String DB_MIGRATION_NEW_EXTENSION = ".new";
    private static final String DB_MIGRATION_OLD_EXTENSION = ".old";
    private static final String MIGRATE_ATTACH_SQL_FORMAT = "ATTACH DATABASE '%s' as rawKeyDb KEY \"x'%s'\";";
    private static final String MIGRATE_CIPHER_SQL = "PRAGMA rawKeyDb.cipher = 'aes-256-cbc';";
    private static final String MIGRATE_EXPORT_SQL = "SELECT sqlcipher_export('rawKeyDb');";
    private static final String MIGRATE_DETACH_SQL = "DETACH DATABASE rawKeyDb;";
    private static final int PASSWORD_KDF_ITER = 10240;

    private Context mContext;
    private WeakReference<Database> mDatabaseWeakReference;
    private LocalKeyMigrationLogger mLocalKeyMigrationLogger;

    public DatabaseLocalKeyMigrator(Context context, Database database, InstallLogRepository installLogRepository) {
        mDatabaseWeakReference = new WeakReference(database);
        mContext = context;
        mLocalKeyMigrationLogger = new LocalKeyMigrationLogger(installLogRepository);
    }

    private String getName() {
        return mDatabaseWeakReference.get().getName();
    }

    public ISQLiteDatabase openAndMigrateToLocalKeyIfNeeded(AppKey appKey, LocalKey localKey,
                                                            boolean migrateIfNeeded) throws SQLException {
        recoverPendingMigration();
        try {
            mLocalKeyMigrationLogger.logSubStep(LocalKeyMigrationLogger.KW_INSTALL_OPEN_DB_RAW_KEY_SUBSTEP);
            ISQLiteDatabase db = getOpenDatabase(localKey);
            mLocalKeyMigrationLogger.logSubStep(LocalKeyMigrationLogger.KW_INSTALL_OPEN_DB_RAW_KEY_SUCCESS_SUBSTEP);
            return db;
        } catch (SQLException e) {
            if (!(appKey instanceof AppKey.Password)) throw e;
            AppKey.Password password = (AppKey.Password) appKey;
            
            ISQLiteDatabase db = getOpenDatabase(password);
            if (migrateIfNeeded) {
                mLocalKeyMigrationLogger
                        .logSubStep(LocalKeyMigrationLogger.KW_INSTALL_MIGRATE_DB_RAW_KEY_SUBSTEP);
                
                migrateFromPasswordToRawKey(password, localKey);
                mLocalKeyMigrationLogger
                        .logSubStep(LocalKeyMigrationLogger.KW_INSTALL_MIGRATE_DB_RAW_KEY_SUCCESS_SUBSTEP);
                return openAndMigrateToLocalKeyIfNeeded(password, localKey, false);
            } else {
                mLocalKeyMigrationLogger
                        .logSubStep(
                                LocalKeyMigrationLogger
                                        .KW_INSTALL_MIGRATE_DB_RAW_KEY_FAIL_PASSWORD_FALLBACK_SUBSTEP);
                return db;
            }
        }
    }

    private ISQLiteDatabase getOpenDatabase(AppKey.Password password) throws SQLException {
        return mDatabaseWeakReference.get()
                                     .openDb(mContext, new Database.PasswordSecret(password, PASSWORD_KDF_ITER),
                                             getName());
    }

    private ISQLiteDatabase getOpenDatabase(LocalKey localKey) throws SQLException {
        return mDatabaseWeakReference.get().openDb(mContext, new Database.RawKey(localKey), getName());
    }

    private void migrateFromPasswordToRawKey(AppKey.Password password, LocalKey localKey) throws SQLException {
        String newName = getName() + DB_MIGRATION_NEW_EXTENSION;
        String oldName = getName() + DB_MIGRATION_OLD_EXTENSION;
        File newDbPath = mContext.getDatabasePath(newName);
        File dbPath = mContext.getDatabasePath(getName());
        File oldDbPath = mContext.getDatabasePath(oldName);
        
        ISQLiteDatabase db = getOpenDatabase(password);
        
        String hexKey = LocalKeyKt.hex(localKey);
        db.rawExecSQL(String.format(MIGRATE_ATTACH_SQL_FORMAT, newDbPath.getPath(), hexKey));
        db.rawExecSQL(MIGRATE_CIPHER_SQL);
        db.rawExecSQL(MIGRATE_EXPORT_SQL);
        db.rawExecSQL(MIGRATE_DETACH_SQL);

        mLocalKeyMigrationLogger
                .logSubStep(LocalKeyMigrationLogger.KW_INSTALL_MIGRATE_DB_RAW_KEY_DUMP_DONE_SUBSTEP);

        
        try {
            mDatabaseWeakReference.get().openDb(mContext, new Database.RawKey(localKey), newName).close();
        } catch (SQLException e) {
            mLocalKeyMigrationLogger
                    .logSubStep(LocalKeyMigrationLogger.KW_INSTALL_MIGRATE_DB_RAW_KEY_READ_FAIL_SUBSTEP);
            
            newDbPath.delete();
            return;
        }

        
        if (!dbPath.renameTo(oldDbPath)) {
            return;
        }
        
        if (!newDbPath.renameTo(dbPath)) {
            recoverPendingMigration();
            return;
        }

        
        oldDbPath.delete();
    }

    private void recoverPendingMigration() {
        File dbPath = mContext.getDatabasePath(getName());
        File newDbPath = mContext.getDatabasePath(getName() + DB_MIGRATION_NEW_EXTENSION);
        File oldDbPath = mContext.getDatabasePath(getName() + DB_MIGRATION_OLD_EXTENSION);
        
        if (newDbPath != null && newDbPath.exists()) {
            mLocalKeyMigrationLogger
                    .logSubStep(LocalKeyMigrationLogger.KW_INSTALL_MIGRATE_DB_RECOVER_CLEANUP_NEW_SUBSTEP);
            newDbPath.delete();
        }
        
        if (oldDbPath != null && oldDbPath.exists()) {
            mLocalKeyMigrationLogger
                    .logSubStep(LocalKeyMigrationLogger.KW_INSTALL_MIGRATE_DB_RECOVER_ROLLBACK_OLD_SUBSTEP);
            oldDbPath.renameTo(dbPath);
        }
    }
}
