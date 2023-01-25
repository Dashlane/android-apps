package com.dashlane.ui.activities.fragments.vault

import android.os.Parcel
import android.os.Parcelable
import com.dashlane.xml.domain.SyncObjectType

enum class Filter(syncObjectTypes: Set<SyncObjectType>) : Set<SyncObjectType> by syncObjectTypes, Parcelable {
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
        SyncObjectType.BANK_STATEMENT
    ),
    FILTER_PASSWORD(
        SyncObjectType.AUTHENTIFIANT
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

    val logTag: String
        get() = when (this) {
            ALL_VISIBLE_VAULT_ITEM_TYPES -> "all_items"
            FILTER_PASSWORD -> "password"
            FILTER_SECURE_NOTE -> "secure_notes"
            FILTER_PAYMENT -> "payments"
            FILTER_PERSONAL_INFO -> "personal_infos"
            FILTER_ID -> "ids"
        }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(ordinal)
    }

    companion object CREATOR : Parcelable.Creator<Filter> {
        override fun createFromParcel(parcel: Parcel): Filter {
            return values()[parcel.readInt()]
        }

        override fun newArray(size: Int): Array<Filter?> {
            return arrayOfNulls(size)
        }
    }
}