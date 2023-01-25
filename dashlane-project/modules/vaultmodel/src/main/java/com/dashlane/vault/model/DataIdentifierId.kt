package com.dashlane.vault.model

import androidx.annotation.IntDef



object DataIdentifierId {

    const val UNKNOWN = -1
    const val ADDRESS = 0
    const val AUTH_CATEGORY = 1
    const val AUTHENTIFIANT = 2
    const val COMPANY = 3
    const val DRIVER_LICENCE = 4
    const val EMAIL = 5
    const val FISCAL_STATEMENT = 6
    const val GENERATED_PASSWORD = 7
    const val ID_CARD = 8
    const val IDENTITY = 9
    const val MERCHANT = 10
    const val PASSPORT = 11
    const val PAYMENT_PAYPAL = 12
    const val PAYMENT_CREDIT_CARD = 13
    const val PERSONAL_DATA_DEFAULT = 14
    const val PERSONAL_WEBSITE = 15
    const val PHONE = 16
    const val PURCHASE_ARTICLE = 17
    const val PURCHASE_BASKET = 18
    const val PURCHASE_CATEGORY = 19
    const val PURCHASE_CONFIRMATION = 20
    const val PURCHASE_PAID_BASKET = 21
    const val SOCIAL_SECURITY_STATEMENT = 22
    const val WEBSITE = 23
    const val SECURE_NOTE = 24
    const val SECURE_NOTE_CATEGORY = 25
    const val BANK_STATEMENT = 26
    const val REACTIVATION_OBJECT = 27
    const val DATA_CHANGE_HISTORY = 32
    const val DATA_USAGE_HISTORY = 33
    const val SECURE_FILE_INFO = 36
    const val SECURITY_BREACH = 37

    @IntDef(
        UNKNOWN,
        ADDRESS,
        AUTH_CATEGORY,
        AUTHENTIFIANT,
        COMPANY,
        DRIVER_LICENCE,
        EMAIL,
        FISCAL_STATEMENT,
        GENERATED_PASSWORD,
        ID_CARD,
        IDENTITY,
        MERCHANT,
        PASSPORT,
        PAYMENT_PAYPAL,
        PAYMENT_CREDIT_CARD,
        PERSONAL_DATA_DEFAULT,
        PERSONAL_WEBSITE,
        PHONE,
        PURCHASE_ARTICLE,
        PURCHASE_BASKET,
        PURCHASE_CATEGORY,
        PURCHASE_CONFIRMATION,
        PURCHASE_PAID_BASKET,
        SOCIAL_SECURITY_STATEMENT,
        WEBSITE,
        SECURE_NOTE,
        SECURE_NOTE_CATEGORY,
        BANK_STATEMENT,
        REACTIVATION_OBJECT,
        DATA_CHANGE_HISTORY,
        DATA_USAGE_HISTORY,
        SECURE_FILE_INFO,
        SECURITY_BREACH
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class Def
}