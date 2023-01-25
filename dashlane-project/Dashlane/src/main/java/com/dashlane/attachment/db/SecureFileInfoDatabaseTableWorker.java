package com.dashlane.attachment.db;

import android.content.ContentValues;
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
import com.dashlane.database.sql.IdCardSql;
import com.dashlane.database.sql.IdentitySql;
import com.dashlane.database.sql.PassportSql;
import com.dashlane.database.sql.PaymentCreditCardSql;
import com.dashlane.database.sql.PaymentPaypalSql;
import com.dashlane.database.sql.PersonalWebsiteSql;
import com.dashlane.database.sql.PhoneSql;
import com.dashlane.database.sql.SecureFileInfoSql;
import com.dashlane.database.sql.SecureNoteSql;
import com.dashlane.database.sql.SocialSecurityStatementSql;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.storage.tableworker.DatabaseTableWorker;
import com.dashlane.util.CursorUtils;
import com.dashlane.vault.model.DataTypeToSql;
import com.dashlane.xml.XmlTransaction;
import com.dashlane.xml.domain.SyncObject;
import com.dashlane.xml.domain.SyncObjectType;
import com.dashlane.xml.serializer.XmlSerialization;

import net.sqlcipher.SQLException;

import androidx.annotation.VisibleForTesting;

import static com.dashlane.xml.domain.SyncObject_XmlKt.toObject;



public class SecureFileInfoDatabaseTableWorker extends DatabaseTableWorker {

    private static final String[] ALL_VERSION_24_TABLES = new String[]{
            
            
            AddressSql.TABLE_NAME,
            AuthCategorySql.TABLE_NAME,
            AuthentifiantSql.TABLE_NAME,
            BankStatementSql.TABLE_NAME,
            CompanySql.TABLE_NAME,
            DataChangeHistorySql.TABLE_NAME,
            DriverLicenceSql.TABLE_NAME,
            EmailSql.TABLE_NAME,
            FiscalStatementSql.TABLE_NAME,
            IdCardSql.TABLE_NAME,
            IdentitySql.TABLE_NAME,
            PassportSql.TABLE_NAME,
            PaymentCreditCardSql.TABLE_NAME,
            PaymentPaypalSql.TABLE_NAME,
            PersonalWebsiteSql.TABLE_NAME,
            PhoneSql.TABLE_NAME,
            SecureNoteSql.TABLE_NAME,
            SocialSecurityStatementSql.TABLE_NAME
    };

    @Override
    public boolean updateDatabaseTables(ISQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            if (oldVersion < 25) {
                createDatabaseTables(db);
                for (String table : ALL_VERSION_24_TABLES) {
                    addAttachmentsToDataIdentifier(db, table);
                }
                migrateAttachments(db, SyncObjectType.SECURE_NOTE);
            }
            if (oldVersion < 40) {
                
                SyncObjectType[] types = SyncObjectType.values();
                for (SyncObjectType type : types) {
                    if (type == SyncObjectType.SECURE_NOTE) {
                        
                        continue;
                    }
                    migrateAttachments(db, type);
                }
            }
            return true;
        } catch (SQLException e) {
            ExceptionLog.v(e);
            return false;
        }
    }

    @Override
    public void createDatabaseTables(ISQLiteDatabase db) {
        db.execSQL(SecureFileInfoSql.DATABASE_CREATE);
    }

    @VisibleForTesting
    void addAttachmentsToDataIdentifier(ISQLiteDatabase db, String table) {
        String sql = getSqlAddColumn(table, DataIdentifierSql.FIELD_ATTACHMENTS, " TEXT DEFAULT '';");
        db.execSQL(sql);
    }

    @VisibleForTesting
    void migrateAttachments(ISQLiteDatabase db, SyncObjectType dataType) {
        
        String tableName = DataTypeToSql.getTableName(dataType);
        Cursor c;
        try {
            c = db.query(tableName,
                         new String[]{DataIdentifierSql.FIELD_ID,
                                      DataIdentifierSql.FIELD_UID,
                                      DataIdentifierSql.FIELD_ATTACHMENTS,
                                      DataIdentifierSql.FIELD_EXTRA},
                         DataIdentifierSql.FIELD_EXTRA + " LIKE ?",
                         new String[]{"%Attachments%"}, null, null, null, null);
        } catch (Exception ignored) {
            
            return;
        }

        XmlSerialization xmlMarshaller = XmlSerialization.Companion;
        if (c != null && c.moveToFirst()) {
            do {
                String identifier = CursorUtils.getString(c, DataIdentifierSql.FIELD_UID);
                String extraData = CursorUtils.getString(c, DataIdentifierSql.FIELD_EXTRA);
                String attachments = CursorUtils.getString(c, DataIdentifierSql.FIELD_ATTACHMENTS);
                
                if (identifier != null && extraData != null && (attachments == null || attachments.isEmpty())) {
                    
                    
                    SyncObject item;
                    try {
                        XmlTransaction syncTransactionXml =
                                xmlMarshaller.deserializeTransaction(extraData);
                        item = toObject(syncTransactionXml, dataType);
                    } catch (Throwable t) {
                        continue;
                    }
                    
                    ContentValues updateValues = new ContentValues(1);
                    updateValues.put(DataIdentifierSql.FIELD_ATTACHMENTS,
                                     item.getAttachments());

                    db.update(tableName, updateValues,
                              DataIdentifierSql.FIELD_ID + " = ?", new String[]{
                                    String.valueOf(item.getId())
                            });
                }
            } while (c.moveToNext());
        }

        CursorUtils.closeCursor(c);
    }

}
