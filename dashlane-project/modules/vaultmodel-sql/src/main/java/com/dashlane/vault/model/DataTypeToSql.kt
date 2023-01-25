@file:JvmName("DataTypeToSql")

package com.dashlane.vault.model

import com.dashlane.database.sql.AddressSql
import com.dashlane.database.sql.AuthCategorySql
import com.dashlane.database.sql.AuthentifiantSql
import com.dashlane.database.sql.BankStatementSql
import com.dashlane.database.sql.CompanySql
import com.dashlane.database.sql.DataChangeHistorySql
import com.dashlane.database.sql.DriverLicenceSql
import com.dashlane.database.sql.EmailSql
import com.dashlane.database.sql.FiscalStatementSql
import com.dashlane.database.sql.GeneratedPasswordSql
import com.dashlane.database.sql.IdCardSql
import com.dashlane.database.sql.IdentitySql
import com.dashlane.database.sql.PassportSql
import com.dashlane.database.sql.PaymentCreditCardSql
import com.dashlane.database.sql.PaymentPaypalSql
import com.dashlane.database.sql.PersonalWebsiteSql
import com.dashlane.database.sql.PhoneSql
import com.dashlane.database.sql.SecureFileInfoSql
import com.dashlane.database.sql.SecureNoteCategorySql
import com.dashlane.database.sql.SecureNoteSql
import com.dashlane.database.sql.SecurityBreachSql
import com.dashlane.database.sql.SocialSecurityStatementSql
import com.dashlane.database.sql.Sql
import com.dashlane.xml.domain.SyncObjectType

fun SyncObjectType.toSql(): Sql? {
    return when (this) {
        SyncObjectType.ADDRESS -> AddressSql
        SyncObjectType.AUTH_CATEGORY -> AuthCategorySql
        SyncObjectType.AUTHENTIFIANT -> AuthentifiantSql
        SyncObjectType.COMPANY -> CompanySql
        SyncObjectType.DRIVER_LICENCE -> DriverLicenceSql
        SyncObjectType.EMAIL -> EmailSql
        SyncObjectType.FISCAL_STATEMENT -> FiscalStatementSql
        SyncObjectType.GENERATED_PASSWORD -> GeneratedPasswordSql
        SyncObjectType.ID_CARD -> IdCardSql
        SyncObjectType.IDENTITY -> IdentitySql
        SyncObjectType.PASSPORT -> PassportSql
        SyncObjectType.PAYMENT_PAYPAL -> PaymentPaypalSql
        SyncObjectType.PAYMENT_CREDIT_CARD -> PaymentCreditCardSql
        SyncObjectType.PERSONAL_WEBSITE -> PersonalWebsiteSql
        SyncObjectType.PHONE -> PhoneSql
        SyncObjectType.SOCIAL_SECURITY_STATEMENT -> SocialSecurityStatementSql
        SyncObjectType.SECURE_NOTE -> SecureNoteSql
        SyncObjectType.SECURE_NOTE_CATEGORY -> SecureNoteCategorySql
        SyncObjectType.BANK_STATEMENT -> BankStatementSql
        SyncObjectType.DATA_CHANGE_HISTORY -> DataChangeHistorySql
        SyncObjectType.SECURE_FILE_INFO -> SecureFileInfoSql
        SyncObjectType.SECURITY_BREACH -> SecurityBreachSql
        else -> null
    }
}

fun SyncObjectType.getTableName() = this.toSql()?.tableName
