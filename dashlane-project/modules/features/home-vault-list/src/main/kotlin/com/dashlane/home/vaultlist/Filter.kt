package com.dashlane.home.vaultlist

import android.os.Parcelable
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.parcelize.Parcelize

@Parcelize
enum class Filter(
    val syncObjectTypes: Set<SyncObjectType>
) : Set<SyncObjectType> by syncObjectTypes, Parcelable {
    ALL_VISIBLE_VAULT_ITEM_TYPES(
        SyncObjectType.ADDRESS,
        SyncObjectType.AUTHENTIFIANT,
        SyncObjectType.COMPANY,
        SyncObjectType.DRIVER_LICENCE,
        SyncObjectType.EMAIL,
        SyncObjectType.FISCAL_STATEMENT,
        SyncObjectType.ID_CARD,
        SyncObjectType.IDENTITY,
        SyncObjectType.PASSPORT,
        SyncObjectType.PAYMENT_PAYPAL,
        SyncObjectType.PAYMENT_CREDIT_CARD,
        SyncObjectType.PERSONAL_WEBSITE,
        SyncObjectType.PHONE,
        SyncObjectType.SOCIAL_SECURITY_STATEMENT,
        SyncObjectType.SECURE_NOTE,
        SyncObjectType.BANK_STATEMENT,
        SyncObjectType.PASSKEY
    ),
    FILTER_PASSWORD(
        SyncObjectType.AUTHENTIFIANT,
        SyncObjectType.PASSKEY
    ),
    FILTER_SECURE_NOTE(
        SyncObjectType.SECURE_NOTE
    ),
    FILTER_PAYMENT(
        SyncObjectType.PAYMENT_CREDIT_CARD,
        SyncObjectType.PAYMENT_PAYPAL,
        SyncObjectType.BANK_STATEMENT
    ),
    FILTER_PERSONAL_INFO(
        SyncObjectType.IDENTITY,
        SyncObjectType.EMAIL,
        SyncObjectType.PHONE,
        SyncObjectType.ADDRESS,
        SyncObjectType.COMPANY,
        SyncObjectType.PERSONAL_WEBSITE
    ),
    FILTER_ID(
        SyncObjectType.ID_CARD,
        SyncObjectType.PASSPORT,
        SyncObjectType.DRIVER_LICENCE,
        SyncObjectType.SOCIAL_SECURITY_STATEMENT,
        SyncObjectType.FISCAL_STATEMENT
    );

    constructor(vararg type: SyncObjectType) : this(type.toSet())
}