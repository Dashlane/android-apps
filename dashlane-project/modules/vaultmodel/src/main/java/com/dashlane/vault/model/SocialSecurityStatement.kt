package com.dashlane.vault.model

import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import java.time.LocalDate

fun createSocialSecurityStatement(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    socialSecurityNumber: String? = null,
    socialSecurityFullname: String? = null,
    linkedIdentity: String? = null,
    dateOfBirth: LocalDate? = null,
    sex: SyncObject.Gender? = null
): VaultItem<SyncObject.SocialSecurityStatement> {
    return dataIdentifier.toVaultItem(
        SyncObject.SocialSecurityStatement {
            this.socialSecurityNumber = socialSecurityNumber?.toSyncObfuscatedValue()
            this.socialSecurityFullname = socialSecurityFullname
            this.linkedIdentity = linkedIdentity
            this.dateOfBirth = dateOfBirth
            this.sex = sex

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

fun VaultItem<SyncObject.SocialSecurityStatement>.copySyncObject(builder: SyncObject.SocialSecurityStatement.Builder.() -> Unit = {}):
        VaultItem<SyncObject.SocialSecurityStatement> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
