package com.dashlane.storage.tableworker;

import com.dashlane.database.ISQLiteDatabase;
import com.dashlane.database.sql.AddressSql;
import com.dashlane.database.sql.AuthCategorySql;
import com.dashlane.database.sql.AuthentifiantSql;
import com.dashlane.database.sql.BankStatementSql;
import com.dashlane.database.sql.ChangeSetChangeSql;
import com.dashlane.database.sql.ChangeSetSql;
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
import com.dashlane.database.sql.SecureNoteCategorySql;
import com.dashlane.database.sql.SecureNoteSql;
import com.dashlane.database.sql.SocialSecurityStatementSql;



public class DeprecatedDatabaseTableWorker extends DatabaseTableWorker {


    private static final String CREATE_VIEW_HISTORY_JOIN_ALL = ("CREATE VIEW IF NOT EXISTS "
            + DataChangeHistorySql.VIEW_HISTORY_JOIN_ALL + " AS "
            + "SELECT * FROM " + DataChangeHistorySql.TABLE_NAME +
            " "
            + "LEFT JOIN " + ChangeSetSql.TABLE_NAME + " ON "
            + DataChangeHistorySql.TABLE_NAME + "." +
            DataIdentifierSql.FIELD_UID +
            " = " +
            ChangeSetSql.TABLE_NAME + "." + ChangeSetSql
            .FIELD_DATA_CHANGE_HISTORY_UID + " "
            + "LEFT JOIN " + ChangeSetChangeSql.TABLE_NAME + " ON "
            + ChangeSetChangeSql.TABLE_NAME + "." +
            ChangeSetChangeSql.FIELD_CHANGESET_UID + " = " +
            ChangeSetSql.TABLE_NAME + "" +
            "." + ChangeSetSql.FIELD_UID
            + ";");


    @Override
    public boolean updateDatabaseTables(ISQLiteDatabase db, int oldVersion, int newVersion) {
        return true;
    }

    @Override
    public void createDatabaseTables(ISQLiteDatabase db) {

        
        db.execSQL(AddressSql.DATABASE_CREATE);
        db.execSQL(AuthentifiantSql.DATABASE_CREATE);
        db.execSQL(DriverLicenceSql.DATABASE_CREATE);
        db.execSQL(PhoneSql.DATABASE_CREATE);
        db.execSQL(SocialSecurityStatementSql.DATABASE_CREATE);
        db.execSQL(EmailSql.DATABASE_CREATE);
        db.execSQL(PassportSql.DATABASE_CREATE);
        db.execSQL(IdCardSql.DATABASE_CREATE);
        db.execSQL(IdentitySql.DATABASE_CREATE);
        db.execSQL(FiscalStatementSql.DATABASE_CREATE);
        db.execSQL(CompanySql.DATABASE_CREATE);
        db.execSQL(PersonalWebsiteSql.DATABASE_CREATE);
        db.execSQL(SecureNoteCategorySql.DATABASE_CREATE);
        db.execSQL(SecureNoteSql.DATABASE_CREATE);
        db.execSQL(BankStatementSql.DATABASE_CREATE);
        db.execSQL(PaymentPaypalSql.DATABASE_CREATE);
        db.execSQL(PaymentCreditCardSql.DATABASE_CREATE);
        db.execSQL(AuthCategorySql.DATABASE_CREATE);
        db.execSQL(GeneratedPasswordSql.DATABASE_CREATE);
        db.execSQL(DataChangeHistorySql.DATABASE_CREATE);
        db.execSQL(ChangeSetSql.DATABASE_CREATE);
        db.execSQL(ChangeSetChangeSql.DATABASE_CREATE);
        db.execSQL(CREATE_VIEW_HISTORY_JOIN_ALL);
    }

}
