package com.dashlane.db;

import android.content.Context;
import android.content.pm.PackageInfo;

import com.dashlane.attachment.db.SecureFileInfoDatabaseTableWorker;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.database.CipherSQLiteDatabaseWrapper;
import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.preference.ConstantsPrefs;
import com.dashlane.preference.GlobalPreferencesManager;
import com.dashlane.storage.tableworker.DataChangeHistoryRevokeTableWorker;
import com.dashlane.storage.tableworker.DatabaseTableWorker;
import com.dashlane.storage.tableworker.DeprecatedDatabaseTableWorker;
import com.dashlane.storage.tableworker.EmergencySharingV1DropTableWorker;
import com.dashlane.storage.tableworker.FiscalStatementFieldsTableWorker;
import com.dashlane.storage.tableworker.SearchQueryCreateTableWorker;
import com.dashlane.storage.tableworker.SharingTableWorker;
import com.dashlane.storage.userdata.IDatabaseUpdateManager;
import com.dashlane.storage.userdata.dao.SharingItemContentDao;
import com.dashlane.storage.userdata.dao.SharingItemGroupDao;
import com.dashlane.storage.userdata.dao.SharingUserGroupDao;
import com.dashlane.teamspaces.db.TeamspaceDatabaseTableWorker;
import com.dashlane.teamspaces.db.TeamspaceIdRetrieverDatabaseTableWorker;
import com.dashlane.teamspaces.db.TeamspaceRemovalTableWorker;
import com.dashlane.useractivity.RacletteLogger;

import net.sqlcipher.database.SQLiteDatabase;

public class UpdateManager implements IDatabaseUpdateManager {
    private final RacletteLogger mLogger;

    public UpdateManager(RacletteLogger logger) {
        mLogger = logger;
    }

    private static final DatabaseTableWorker[] DB_WORKERS = {
            new DeprecatedDatabaseTableWorker(),
            new TeamspaceDatabaseTableWorker(),
            new TeamspaceIdRetrieverDatabaseTableWorker(),
            new TeamspaceRemovalTableWorker(),
            new DataChangeHistoryRevokeTableWorker(),
            new SharingItemGroupDao.TableWorker(),
            new OtpSecretDatabaseTableWorker(),
            new OtpUrlDatabaseTableWorker(),
            new SharingTableWorker(),
            new SecurityBreachTableWorker(),
            new SharingItemContentDao.TableWorker(),
            new SharingUserGroupDao.TableWorker(),
            new SecureFileInfoDatabaseTableWorker(),
            new CreationModificationDateTableWorker(),
            new SecureNoteDateRectificationTableWorker(),
            new EmergencySharingV1DropTableWorker(),
            new BackupDateTableWorker(),
            new SchemaConsistencyTableWorker(),
            new SearchQueryCreateTableWorker(),
            new XmlBackupFixTableWorker(),
            new UserDataHashTableWorker(),
            new LocallyUsedCountTableWorker(),
            new TitleRawTableWorker(),
            new PasswordGeneratorPlatformTableWorker(),
            new FiscalStatementFieldsTableWorker(),
            new LinkedServicesDatabaseTableWorker()
    };

    public static boolean shouldDoUpdate(Context context) {
        GlobalPreferencesManager preferencesManager = SingletonProvider.getGlobalPreferencesManager();
        int runningCode = preferencesManager.getInt(ConstantsPrefs.RUNNING_VERSION); 
        
        int versionCode = 0;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode; 
        } catch (Exception e) {
            ExceptionLog.v(e);
        }
        if (versionCode > runningCode) {
            preferencesManager.putInt(ConstantsPrefs.RUNNING_VERSION, versionCode);
            return true;
        } else {
            return false;
        }
    }

    public static void setFirstRunVersionCode(Context context) {
        GlobalPreferencesManager preferenceManager = SingletonProvider.getGlobalPreferencesManager();
        if (preferenceManager.exist(ConstantsPrefs.FIRST_RUN_VERSION_CODE)) {
            return; 
        }
        int versionCode;
        if (preferenceManager.shouldSkipIntro()) {
            versionCode = 0; 
        } else {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                versionCode = packageInfo.versionCode;
                
            } catch (Exception e) {
                ExceptionLog.v(e);
                versionCode = 0;
            }
        }
        preferenceManager.putInt(ConstantsPrefs.FIRST_RUN_VERSION_CODE, versionCode);
    }

    @Override
    public void createDatabase(SQLiteDatabase db) {
        createDatabase(new CipherSQLiteDatabaseWrapper(db));
    }

    @Override
    public void migrateDatabase(net.sqlcipher.database.SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            migrateDatabase(new CipherSQLiteDatabaseWrapper(db), oldVersion, newVersion);
        } catch (Exception ex) {
            mLogger.legacyMigrationFailure(ex);
            throw ex;
        }
    }

    private static void createDatabase(ISQLiteDatabase db) {
        for (DatabaseTableWorker databaseTableWorker : DB_WORKERS) {
            databaseTableWorker.createDatabaseTables(db);
        }
    }

    private static void migrateDatabase(ISQLiteDatabase db, int oldVersion, int newVersion) {
        for (DatabaseTableWorker databaseTableWorker : DB_WORKERS) {
            databaseTableWorker.updateDatabaseTables(db, oldVersion, newVersion);
        }
    }
}