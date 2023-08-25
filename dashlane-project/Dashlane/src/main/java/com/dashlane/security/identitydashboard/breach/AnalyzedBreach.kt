package com.dashlane.security.identitydashboard.breach

import android.annotation.SuppressLint
import android.os.Parcelable
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.leakedPasswordsSet
import com.dashlane.xml.domain.SyncObject
import kotlinx.parcelize.Parcelize

fun VaultItem<SyncObject.SecurityBreach>.toAnalyzedBreach() = AnalyzedBreach(
    syncObject.breachId,
    syncObject.content,
    syncObject.leakedPasswordsSet,
    syncObject.status,
    uid
)

@SuppressLint("ParcelCreator")
@Parcelize
data class AnalyzedBreach(
    val breachId: String?,
    val content: String?,
    val leakedPasswordsSet: Set<String>,
    val status: SyncObject.SecurityBreach.Status?,
    val uid: String
) : Parcelable {
    val viewed: Boolean
        get() = status != SyncObject.SecurityBreach.Status.PENDING
    val solved: Boolean
        get() = status == SyncObject.SecurityBreach.Status.SOLVED
}