package com.dashlane.db;

import android.database.Cursor;

import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.database.sql.AddressSql;
import com.dashlane.database.sql.AuthCategorySql;
import com.dashlane.database.sql.AuthentifiantSql;
import com.dashlane.database.sql.BankStatementSql;
import com.dashlane.database.sql.CompanySql;
import com.dashlane.database.sql.DataChangeHistorySql;
import com.dashlane.database.sql.DataIdentifierSql;
import com.dashlane.database.sql.DriverLicenceSql;
import com.dashlane.database.sql.EmailSql;
import com.dashlane.database.sql.FiscalStatementSql;
import com.dashlane.database.sql.GeneratedPasswordSql;
import com.dashlane.database.sql.IdCardSql;
import com.dashlane.database.sql.IdentitySql;
import com.dashlane.database.sql.PassportSql;
import com.dashlane.database.sql.PaymentCreditCardSql;
import com.dashlane.database.sql.PaymentPaypalSql;
import com.dashlane.database.sql.PersonalWebsiteSql;
import com.dashlane.database.sql.PhoneSql;
import com.dashlane.database.sql.SecureFileInfoSql;
import com.dashlane.database.sql.SecureNoteCategorySql;
import com.dashlane.database.sql.SecureNoteSql;
import com.dashlane.database.sql.SecurityBreachSql;
import com.dashlane.database.sql.SocialSecurityStatementSql;
import com.dashlane.storage.tableworker.DatabaseTableWorker;
import com.dashlane.util.CursorUtils;
import com.dashlane.vault.model.DataTypeToSql;
import com.dashlane.vault.util.SyncObjectTypeUtils;
import com.dashlane.xml.domain.SyncObjectType;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.VisibleForTesting;

import static com.dashlane.util.CursorUtils.getInt;
import static com.dashlane.util.CursorUtils.getLong;
import static com.dashlane.util.CursorUtils.getString;




@SuppressWarnings("squid:S1192")
public class CreationModificationDateTableWorker extends DatabaseTableWorker {
    private static final int RECENT_ITEMS_TABLE_WORKER_VERSION = 19;
    private static final int RECENT_FIELD_SUPPORT_VERSION = 27;
    private static final int MISSING_RECENT_FIELD_SUPPORT_VERSION = 28;
    private static final int LOCALLY_VIEWED_DATE_SUPPORT_VERSION = 30;
    private static final int MISSING_LOCALLY_VIEWED_FIELD_SUPPORT_VERSION = 31;


    private static final String FIELD_SECURE_NOTE_DEPRECATED_DATE_CREATED = "dateCreated";
    private static final String FIELD_SECURE_NOTE_DEPRECATED_DATE_EDITED = "dateEdited";
    private static final String FIELD_AUTHENTIFIANT_DEPRECATED_AUTH_CREATION_DATE = "auth_creation_date";
    private static final String FIELD_AUTHENTIFIANT_DEPRECATED_MODIFICATION_DATE = "auth_modification_date";
    private static final String FIELD_SECURE_FILE_INFO_DEPRECATED_CREATION_DATE_TIME = "creationTime";
    private static final String FIELD_SECURE_FILE_INFO_DEPRECATED_UPDATE_DATE_TIME = "updateTime";

    
    private static final String RECENT_ITEM_TABLE_NAME = "RecentItem";
    private static final String COLUMN_ACCESS_TIMESTAMP = "access_timestamp";
    private static final String COLUMN_DATATYPE = "data_type";
    private static final String COLUMN_ACCESS_STATE = "access_state";
    private static final String COLUMN_DATA_UID = DataIdentifierSql.FIELD_UID;

    private static final String[] ALL_VERSION_27_TABLES = new String[]{
            AddressSql.TABLE_NAME,
            AuthentifiantSql.TABLE_NAME,
            BankStatementSql.TABLE_NAME,
            CompanySql.TABLE_NAME,
            DataChangeHistorySql.TABLE_NAME,
            DriverLicenceSql.TABLE_NAME,
            EmailSql.TABLE_NAME,
            FiscalStatementSql.TABLE_NAME,
            IdCardSql.TABLE_NAME,
            IdentitySql.TABLE_NAME,
            PaymentCreditCardSql.TABLE_NAME,
            PassportSql.TABLE_NAME,
            PaymentPaypalSql.TABLE_NAME,
            PersonalWebsiteSql.TABLE_NAME,
            PhoneSql.TABLE_NAME,
            SecureNoteSql.TABLE_NAME,
            SocialSecurityStatementSql.TABLE_NAME
    };

    private static final String[] MISSING_TABLES_TO_MIGRATE = new String[]{
            AuthCategorySql.TABLE_NAME,
            GeneratedPasswordSql.TABLE_NAME,
            SecureNoteCategorySql.TABLE_NAME
    };

    private @interface AccessType {
        int READ = 1;
        int MODIFY = 2;
        int CREATE = 3;
    }

    @Override
    public boolean updateDatabaseTables(ISQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < RECENT_FIELD_SUPPORT_VERSION) {
            addCreateAndModificationDate(db);
        }
        
        if (oldVersion < MISSING_RECENT_FIELD_SUPPORT_VERSION) {
            addCreateAndModificationDateMissingTables(db);
        }
        
        if (oldVersion < LOCALLY_VIEWED_DATE_SUPPORT_VERSION) {
            callAddLocallyViewedColumnForAllDataType(db);
        }
        
        if (RECENT_ITEMS_TABLE_WORKER_VERSION < oldVersion && oldVersion < LOCALLY_VIEWED_DATE_SUPPORT_VERSION) {
            callRecentItemTableContentMigration(db);
        }
        if (oldVersion < MISSING_LOCALLY_VIEWED_FIELD_SUPPORT_VERSION) {
            callAddLocallyViewedColumnForAllDataTypeMissingTable(db);
        }
        if (oldVersion < MISSING_LOCALLY_VIEWED_FIELD_SUPPORT_VERSION && oldVersion > 29) {
            
            
            addLocallyViewedDate(db, SecurityBreachSql.TABLE_NAME);
        }
        return true;
    }


    @VisibleForTesting
    void callAddLocallyViewedColumnForAllDataType(ISQLiteDatabase db) {
        for (String table : ALL_VERSION_27_TABLES) {
            addLocallyViewedDate(db, table);
        }
    }

    private void callAddLocallyViewedColumnForAllDataTypeMissingTable(ISQLiteDatabase db) {
        for (String table : MISSING_TABLES_TO_MIGRATE) {
            addLocallyViewedDate(db, table);
        }
    }

    @VisibleForTesting
    void addCreateAndModificationDate(ISQLiteDatabase db) {
        for (String table : ALL_VERSION_27_TABLES) {
            
            addCreationDate(db, table);
            addUserModificationDate(db, table);

            
            
            if (AuthentifiantSql.TABLE_NAME.equals(table)) {
                copyColumnInTable(db, table, FIELD_AUTHENTIFIANT_DEPRECATED_AUTH_CREATION_DATE,
                                  DataIdentifierSql.FIELD_CREATION_DATE);
                copyColumnInTable(db, table, FIELD_AUTHENTIFIANT_DEPRECATED_MODIFICATION_DATE,
                                  DataIdentifierSql.FIELD_USER_MODIFICATION_DATE);

                addAuthentifiantPasswordModificationDate(db);
                copyColumnInTable(db, table, FIELD_AUTHENTIFIANT_DEPRECATED_MODIFICATION_DATE,
                                  AuthentifiantSql.FIELD_AUTH_PASSWORD_MODIFICATION_DATE);

            } else if (SecureNoteSql.TABLE_NAME.equals(table)) {
                copyColumnInTable(db, table, FIELD_SECURE_NOTE_DEPRECATED_DATE_CREATED,
                                  DataIdentifierSql.FIELD_CREATION_DATE);
                copyColumnInTable(db, table, FIELD_SECURE_NOTE_DEPRECATED_DATE_EDITED,
                                  DataIdentifierSql.FIELD_USER_MODIFICATION_DATE);
            } else if (SecureFileInfoSql.TABLE_NAME.equals(table)) {
                copyColumnInTable(db, table, FIELD_SECURE_FILE_INFO_DEPRECATED_CREATION_DATE_TIME,
                                  DataIdentifierSql.FIELD_CREATION_DATE);
                copyColumnInTable(db, table, FIELD_SECURE_FILE_INFO_DEPRECATED_UPDATE_DATE_TIME,
                                  DataIdentifierSql.FIELD_USER_MODIFICATION_DATE);
            }
        }
    }

    @VisibleForTesting
    void callRecentItemTableContentMigration(ISQLiteDatabase database) {
        
        Cursor cursor = getCursorForRecentItems(database);

        
        if (cursor == null) return;

        Map<String, PendingUpdateItem> myList = new HashMap<>();

        while (cursor.moveToNext()) {
            
            String uid = getString(cursor, COLUMN_DATA_UID);
            SyncObjectType dataType = SyncObjectTypeUtils.valueFromDesktopId(getInt(cursor, COLUMN_DATATYPE));
            int accessType = getInt(cursor, COLUMN_ACCESS_STATE);
            long accessDate = getLong(cursor, COLUMN_ACCESS_TIMESTAMP);

            
            if (!myList.containsKey(uid)) {
                myList.put(uid, new PendingUpdateItem(dataType));
            }

            switch (accessType) {
                case AccessType.READ:
                    myList.get(uid).mLocallyViewdDate = Long.toString(accessDate);
                    break;
                case AccessType.MODIFY:
                    myList.get(uid).mUserModificationDate = Long.toString(accessDate);
                    break;
                case AccessType.CREATE:
                    myList.get(uid).mCreationDate = Long.toString(accessDate);
                    break;
            }

        }
        CursorUtils.closeCursor(cursor);

        
        for (int i = 0; i < myList.size(); i++) {
            String uid = (String) myList.keySet().toArray()[i];
            updateItem(database, uid, myList.get(uid));
        }

        
        database.execSQL("DROP TABLE IF EXISTS " + RECENT_ITEM_TABLE_NAME);
    }

    @VisibleForTesting
    void addCreateAndModificationDateMissingTables(ISQLiteDatabase db) {
        for (String table : MISSING_TABLES_TO_MIGRATE) {
            
            addCreationDate(db, table);
            addUserModificationDate(db, table);
        }
    }

    @Override
    public void createDatabaseTables(ISQLiteDatabase db) {
        
    }


    @VisibleForTesting
    void addCreationDate(ISQLiteDatabase db, String tableName) {
        
        String creationDate = DataIdentifierSql.FIELD_CREATION_DATE;
        String sql = getSqlAddColumn(tableName, creationDate, "TEXT DEFAULT \'0\';");

        db.execSQL(sql);
    }

    @VisibleForTesting
    void addUserModificationDate(ISQLiteDatabase db, String tableName) {
        
        String userModificationDate = DataIdentifierSql.FIELD_USER_MODIFICATION_DATE;
        String sql = getSqlAddColumn(tableName, userModificationDate, "TEXT DEFAULT \'0\';");

        db.execSQL(sql);
    }

    @VisibleForTesting
    void addLocallyViewedDate(ISQLiteDatabase db, String tableName) {
        
        String locallyViewed = DataIdentifierSql.FIELD_LOCALLY_VIEWED_DATE;
        String sql = getSqlAddColumn(tableName, locallyViewed, "TEXT DEFAULT \'0\';");

        db.execSQL(sql);
    }

    @VisibleForTesting
    public void copyColumnInTable(ISQLiteDatabase db, String tableName, String sourceColumn, String destColumn) {
        String sql = getSqlCopyColumn(tableName, sourceColumn, destColumn);

        db.execSQL(sql);
    }

    @VisibleForTesting
    public void addAuthentifiantPasswordModificationDate(ISQLiteDatabase db) {
        String tableName = AuthentifiantSql.TABLE_NAME;

        String authPasswordModificationDate = AuthentifiantSql.FIELD_AUTH_PASSWORD_MODIFICATION_DATE;
        String sql = getSqlAddColumn(tableName, authPasswordModificationDate, "TEXT DEFAULT \'0\';");

        db.execSQL(sql);
    }


    

    private void updateItem(ISQLiteDatabase database, String uid, PendingUpdateItem pendingUpdateItem) {
        String sql = pendingUpdateItem.getSqliteUpdateQuery(uid);
        database.execSQL(sql);
    }

    

    private Cursor getCursorForRecentItems(ISQLiteDatabase db) {
        String[] columns = new String[]{"*"};
        String orderBy = COLUMN_ACCESS_TIMESTAMP + " DESC";

        if (db == null) return null;
        try {
            return db.query(RECENT_ITEM_TABLE_NAME, columns, null, null, null, null, orderBy, null);
        } catch (Exception e) {
            return null;
        }
    }

    

    private static class PendingUpdateItem {
        private SyncObjectType mDataType;
        private String mCreationDate;
        private String mUserModificationDate;
        private String mLocallyViewdDate;

        PendingUpdateItem(SyncObjectType dataType) {
            this(dataType, "0", "0", "0");
        }

        PendingUpdateItem(SyncObjectType dataType, String creationDate, String userModificationDate,
                          String locallyViewedDate) {
            mDataType = dataType;
            mCreationDate = creationDate;
            mUserModificationDate = userModificationDate;
            mLocallyViewdDate = locallyViewedDate;
        }

        

        String getSqliteUpdateQuery(String uid) {
            return "UPDATE " + DataTypeToSql.getTableName(mDataType) + " SET "
                   + DataIdentifierSql.FIELD_CREATION_DATE + " = " + mCreationDate
                   + ", " + DataIdentifierSql.FIELD_USER_MODIFICATION_DATE + " = " + mUserModificationDate
                   + ", " + DataIdentifierSql.FIELD_LOCALLY_VIEWED_DATE + " = " + mLocallyViewdDate
                   + " WHERE uid = '" + uid + "';";
        }
    }
}