package com.dashlane.vault.model

import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import java.time.LocalDate

fun createIdentity(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    type: String? = null,
    
    title: SyncObject.Identity.Title? = null,
    firstname: String? = null,
    lastname: String? = null,
    middlename: String? = null,
    pseudo: String? = null,
    dateOfBirth: LocalDate? = null,
    placeOfBirth: String? = null
): VaultItem<SyncObject.Identity> {
    return dataIdentifier.toVaultItem(
        SyncObject.Identity {
            this.type = type
            this.title = title
            this.firstName = firstname
            this.lastName = lastname
            this.middleName = middlename
            this.pseudo = pseudo
            this.birthDate = dateOfBirth
            this.birthPlace = placeOfBirth

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

val SummaryObject.Identity.identityPartialOrFullNameNoLogin: String?
    get() {
        val firstname = firstName ?: ""
        val lastname = lastName ?: ""
        val value = "$firstname $lastname"
        return value.takeIf { it.isNotSemanticallyNull() }
    }

fun VaultItem<SyncObject.Identity>.copySyncObject(builder: SyncObject.Identity.Builder.() -> Unit = {}):
        VaultItem<SyncObject.Identity> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
