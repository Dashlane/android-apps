package com.dashlane.sharing

import android.os.Parcelable
import com.dashlane.server.api.endpoints.sharinguserdevice.ItemForEmailing
import com.dashlane.url.root
import com.dashlane.url.toUrlOrNull
import com.dashlane.vault.model.navigationUrl
import com.dashlane.vault.summary.SummaryObject
import kotlinx.parcelize.Parcelize

@Parcelize
data class SharedVaultItemLite(
    val uid: String,
    val xmlObjectName: String,
    val website: String?,
    val title: String,
    val itemType: ItemForEmailing.Type,
) : Parcelable {
    constructor(summaryObject: SummaryObject) : this(
        uid = summaryObject.id,
        xmlObjectName = summaryObject.syncObjectType.xmlObjectName,
        website = summaryObject.let {
            if (it is SummaryObject.Authentifiant) {
                it.navigationUrl?.toUrlOrNull()?.root
            } else {
                null
            }
        },
        title = summaryObject.let {
            when (it) {
                is SummaryObject.Authentifiant -> it.title
                is SummaryObject.SecureNote -> it.title
                is SummaryObject.Secret -> it.title
                else -> error("Not a sharable type ${summaryObject.syncObjectType}")
            }
        } ?: "",
        itemType = summaryObject.let {
            when (it) {
                is SummaryObject.Authentifiant -> ItemForEmailing.Type.PASSWORD
                is SummaryObject.SecureNote -> ItemForEmailing.Type.NOTE
                is SummaryObject.Secret -> ItemForEmailing.Type.SECRET
                else -> error("Not a sharable type ${summaryObject.syncObjectType}")
            }
        },
    )
}

fun SummaryObject.toSharedVaultItemLite() = SharedVaultItemLite(this)

fun SharedVaultItemLite.toItemForEmailing(): ItemForEmailing = ItemForEmailing(title, itemType)