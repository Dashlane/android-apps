package com.dashlane.storage.userdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.dashlane.CipherDatabaseUtils;
import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.session.AppKey;
import com.dashlane.session.AppKeyKt;
import com.dashlane.session.LocalKey;
import com.dashlane.session.LocalKeyKt;
import com.dashlane.useractivity.log.install.InstallLogRepository;

import net.sqlcipher.SQLException;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import kotlin.collections.ArraysKt;

public abstract class Database {

    public static final String ENABLE_FOREIGN_KEY_QUERY = "PRAGMA foreign_keys=ON;";
    public static final String DEFER_FOREIGN_KEYS_QUERY = "PRAGMA defer_foreign_keys=ON;";
    public static final String DISABLE_SYNC_WRITE = "PRAGMA synchronous=0;";

    private static final String SQL_PRAGMA_RAW_KEY_FORMAT = "PRAGMA KEY = \"x'%s'\";";
    private static final String SQL_PRAGMA_PASSWORD_FORMAT = "PRAGMA key = '%s';";
    private static final String SQL_PRAGMA_KDF_ITER_FORMAT = "PRAGMA kdf_iter = '%d';";
    

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    static final int DATABASE_VERSION = 48;

    @VisibleForTesting
    ISQLiteDatabase mDb;

    IDatabaseUpdateManager mUpdateManager;
    private final InstallLogRepository mInstallLogRepository;

    private final Context mCtx;
    private final String mDbName;

    private final Set<OnUpdateListener> mOnUpdateListeners = new HashSet<>();

    public Database(Context context, String username,
                    IDatabaseUpdateManager updateManager,
                    InstallLogRepository installLogRepository) {
        mCtx = context;

        mDbName = CipherDatabaseUtils.getDatabaseName(username);
        mUpdateManager = updateManager;
        mInstallLogRepository = installLogRepository;
    }

    public Database open(AppKey password, LocalKey localKey) throws SQLException {
        DatabaseLocalKeyMigrator dbMigrator = new DatabaseLocalKeyMigrator(mCtx, this, mInstallLogRepository);
        mDb = dbMigrator.openAndMigrateToLocalKeyIfNeeded(password, localKey, true);
        mDb.execSQL(ENABLE_FOREIGN_KEY_QUERY);
        mDb.execSQL(DISABLE_SYNC_WRITE);
        return this;
    }

    public boolean isDecryptable(Secret secret) {
        try {
            openDb(mCtx, secret).close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isOpen() {
        return mDb != null && mDb.isOpen();
    }

    public void close() {
        mDb.close();
    }

    public long insert(String tableName, ContentValues cv) {
        long id = mDb.insert(tableName, null, cv);
        notifyUpdateListeners();
        return id;
    }

    public int delete(String tablename, String selection, String[] selectionArgs) {
        int count = mDb.delete(tablename, selection, selectionArgs);
        notifyUpdateListeners();
        return count;
    }

    public int update(String tablename, ContentValues values, String selection, String[] selectionArgs) {
        int count = mDb.update(tablename, values, selection, selectionArgs);
        notifyUpdateListeners();
        return count;
    }

    

    @Deprecated
    public Cursor query(String tablename, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        return query(tablename, projection, selection, selectionArgs, null, null, sortOrder, null);
    }

    public Cursor query(@NonNull SqlQuery sqlQuery) {
        String table = sqlQuery.getTable();
        String[] columns = sqlQuery.getColumnsAsArray();
        String selection = sqlQuery.getSelection();
        String[] selectionArgs = sqlQuery.getSelectionArgsAsArray();
        String groupBy = sqlQuery.getGroupBy();
        String having = sqlQuery.getHaving();
        String orderBy = sqlQuery.getOrderBy();
        String limit = sqlQuery.getLimit();

        return query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    @Nullable
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy,
                        String having, String orderBy, String limit) {
        if (orderBy == null && (columns == null || ArraysKt.contains(columns, BaseColumns._ID))) {
            orderBy = BaseColumns._ID + " DESC";
        }
        try {
            return mDb.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);
        } catch (Exception e) {
            return null;
        }
    }

    public int count(String tableName) {
        Cursor c = mDb.rawQuery("select count(*) from " + tableName, null);
        c.moveToFirst();
        int i = c.getInt(0);
        c.close();
        return i;
    }

    public String getName() {
        return mDbName;
    }

    public int getVersion() {
        return mDb.getVersion();
    }

    public void executeNativeSQL(String sql) {
        mDb.execSQL(sql);
        notifyUpdateListeners();
    }

    public void executeRawExecSQL(String sql) {
        mDb.rawExecSQL(sql);
        notifyUpdateListeners();
    }

    public void beginTransaction() {
        mDb.beginTransaction();
        mDb.execSQL(DEFER_FOREIGN_KEYS_QUERY);
    }

    public void setTransactionSuccessful() {
        mDb.setTransactionSuccessful();
    }

    public void commit() {
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
    }

    public Cursor rawQuery(String sql, String[] selectionArgs) {
        return mDb.rawQuery(sql, selectionArgs);
    }

    public boolean inTransaction() {
        return mDb.inTransaction();
    }

    public void endTransaction() {
        mDb.endTransaction();
        notifyUpdateListeners();
    }

    public long insertWithOnConflict(String table, String nullColumnHack, ContentValues initialValues,
                                     int conflictAlgorithm) {
        long id = mDb.insertWithOnConflict(table, nullColumnHack, initialValues, conflictAlgorithm);
        if (!inTransaction()) {
            notifyUpdateListeners();
        }
        return id;
    }

    public void addListener(OnUpdateListener listener) {
        mOnUpdateListeners.add(listener);
    }

    public void removeListener(OnUpdateListener listener) {
        mOnUpdateListeners.remove(listener);
    }

    @VisibleForTesting
    ISQLiteDatabase openDb(Context context, Secret secret) {
        return openDb(context, secret, getName());
    }

    public abstract ISQLiteDatabase openDb(Context context, Secret secret, String name);

    private void notifyUpdateListeners() {
        for (OnUpdateListener updateListener : mOnUpdateListeners) {
            updateListener.onInsertOrUpdate(this);
        }
    }

    public interface OnUpdateListener {
        void onInsertOrUpdate(Database database);
    }

    public static abstract class Secret {
        public abstract String[] getSqlCommands();
    }

    public static class PasswordSecret extends Secret {
        private final AppKey.Password mPassword;
        private final int mIterations;

        public PasswordSecret(AppKey.Password password, int kdfIterations) {
            mPassword = password;
            mIterations = kdfIterations;
        }

        @Override
        public String[] getSqlCommands() {
            String passSql =
                    String.format(SQL_PRAGMA_PASSWORD_FORMAT,
                            escapeSqlPragmaPass(AppKeyKt.decodeCryptographyKeyUtf8ToString(mPassword)));
            String iterSql = String.format(Locale.US, SQL_PRAGMA_KDF_ITER_FORMAT, mIterations);
            return new String[]{passSql, iterSql};
        }

        private String escapeSqlPragmaPass(String password) {
            String escapedpassword = password;
            try {
                escapedpassword = password.replace("'", "^");
            } catch (Exception e) {
                
            }
            return escapedpassword;
        }
    }

    public static class RawKey extends Secret {
        private final LocalKey mLocalKey;

        public RawKey(LocalKey localKey) {
            mLocalKey = localKey;
        }

        @Override
        public String[] getSqlCommands() {
            String keySql = String.format(SQL_PRAGMA_RAW_KEY_FORMAT, LocalKeyKt.hex(mLocalKey));
            return new String[]{keySql};
        }
    }

}
