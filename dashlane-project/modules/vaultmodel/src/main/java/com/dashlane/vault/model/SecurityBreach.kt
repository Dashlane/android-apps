package com.dashlane.vault.model

import com.dashlane.util.asOptStringSequence
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import org.json.JSONArray

fun createSecurityBreach(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    breachId: String,
    content: String? = null,
    contentRevision: Int = 0,
    status: SyncObject.SecurityBreach.Status?,
    leakedPasswords: Set<String> = emptySet()
): VaultItem<SyncObject.SecurityBreach> {
    return dataIdentifier.toVaultItem(
        SyncObject.SecurityBreach {
            this.breachId = breachId
            this.content = content
            this.contentRevision = contentRevision.toLong()
            this.status = status
            this.leakedPasswords = serialize(leakedPasswords).toSyncObfuscatedValue()

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        }
    )
}

var SyncObject.SecurityBreach.Builder.leakedPasswordsSet: Set<String>?
    get() = unserialize(this.leakedPasswords?.toString())
    set(value) {
        this.leakedPasswords = serialize(value).toSyncObfuscatedValue()
    }

val SyncObject.SecurityBreach.leakedPasswordsSet: Set<String>
    get() = unserialize(this.leakedPasswords?.toString())
fun SyncObject.SecurityBreach.isViewed() =
    status == SyncObject.SecurityBreach.Status.VIEWED || status == SyncObject.SecurityBreach.Status.ACKNOWLEDGED

private fun serialize(set: Set<String>?): String {
    if (set == null) return JSONArray().toString()

    val jsonArray = JSONArray()
    set.forEach { jsonArray.put(it) }
    return jsonArray.toString()
}

private fun unserialize(serialized: String?): Set<String> {
    if (serialized == null) return setOf()

    return try {
        val jsonArray = JSONArray(serialized)
        jsonArray.asOptStringSequence().filterNotNull().toSet()
    } catch (e: Exception) {
        setOf()
    }
}

fun VaultItem<SyncObject.SecurityBreach>.copySyncObject(builder: SyncObject.SecurityBreach.Builder.() -> Unit = {}):
        VaultItem<SyncObject.SecurityBreach> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
