package com.dashlane.xml.domain

object SyncObjectTypeUtils {
    val WITH_TEAMSPACES
        get() = setOf(
            SyncObjectType.ADDRESS,
            SyncObjectType.AUTH_CATEGORY,
            SyncObjectType.AUTHENTIFIANT,
            SyncObjectType.COMPANY,
            SyncObjectType.COLLECTION,
            SyncObjectType.DRIVER_LICENCE,
            SyncObjectType.EMAIL,
            SyncObjectType.FISCAL_STATEMENT,
            SyncObjectType.GENERATED_PASSWORD,
            SyncObjectType.ID_CARD,
            SyncObjectType.IDENTITY,
            SyncObjectType.PASSKEY,
            SyncObjectType.PASSPORT,
            SyncObjectType.PAYMENT_CREDIT_CARD,
            SyncObjectType.PERSONAL_WEBSITE,
            SyncObjectType.PHONE,
            SyncObjectType.SOCIAL_SECURITY_STATEMENT,
            SyncObjectType.SECRET,
            SyncObjectType.SECURE_NOTE,
            SyncObjectType.SECURE_NOTE_CATEGORY,
            SyncObjectType.BANK_STATEMENT,
            SyncObjectType.SECURE_FILE_INFO,
            SyncObjectType.WI_FI,
        )

    val SHAREABLE: Set<SyncObjectType>
        get() = setOf(
            SyncObjectType.AUTHENTIFIANT,
            SyncObjectType.SECRET,
            SyncObjectType.SECURE_NOTE,
        )
}

val SyncObjectType.isSupportedSyncObjectType: Boolean
    get() = when (this) {
        SyncObjectType.ADDRESS,
        SyncObjectType.AUTH_CATEGORY,
        SyncObjectType.AUTHENTIFIANT,
        SyncObjectType.BANK_STATEMENT,
        SyncObjectType.COMPANY,
        SyncObjectType.COLLECTION,
        SyncObjectType.DATA_CHANGE_HISTORY,
        SyncObjectType.DRIVER_LICENCE,
        SyncObjectType.EMAIL,
        SyncObjectType.FISCAL_STATEMENT,
        SyncObjectType.GENERATED_PASSWORD,
        SyncObjectType.ID_CARD,
        SyncObjectType.IDENTITY,
        SyncObjectType.PASSKEY,
        SyncObjectType.PASSPORT,
        SyncObjectType.PAYMENT_CREDIT_CARD,
        SyncObjectType.PERSONAL_WEBSITE,
        SyncObjectType.PHONE,
        SyncObjectType.SECRET,
        SyncObjectType.SECURE_FILE_INFO,
        SyncObjectType.SECURE_NOTE,
        SyncObjectType.SECURE_NOTE_CATEGORY,
        SyncObjectType.SECURITY_BREACH,
        SyncObjectType.SOCIAL_SECURITY_STATEMENT,
        SyncObjectType.WI_FI,
        -> true

        else -> false
    }

val SyncObject.isSupportedSyncObjectType: Boolean
    get() = objectType.isSupportedSyncObjectType