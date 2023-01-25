package com.dashlane.vault.model

import android.content.Context
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

fun createPhone(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    type: SyncObject.Phone.Type? = null,
    phoneNumber: String? = null,
    phoneName: String? = ""
): VaultItem<SyncObject.Phone> {
    return dataIdentifier.toVaultItem(
        SyncObject.Phone {
            this.type = type
            this.number = phoneNumber
            this.phoneName = phoneName

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

fun SummaryObject.Phone.getPhoneNameAndNumber(context: Context): String {
    val builder = StringBuilder()
    builder.append(if (phoneName.isNotSemanticallyNull()) phoneName else context.getString(R.string.phone))
    return builder.toString()
}

fun VaultItem<SyncObject.Phone>.copySyncObject(builder: SyncObject.Phone.Builder.() -> Unit = {}):
        VaultItem<SyncObject.Phone> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
