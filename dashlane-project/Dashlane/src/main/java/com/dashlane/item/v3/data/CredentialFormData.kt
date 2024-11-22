package com.dashlane.item.v3.data

import com.dashlane.authenticator.Otp
import com.dashlane.ext.application.KnownLinkedDomains
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.securefile.extensions.attachmentsCount
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.model.urlDomain
import com.dashlane.vault.model.urlForUI
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObfuscatedValue

data class CredentialFormData(
    val email: String? = null,
    val login: String? = null,
    val secondaryLogin: String? = null,
    val url: String? = null,
    val note: String? = null,
    val linkedServices: LinkedServices = LinkedServices(),
    val passwordHealth: PasswordHealthData? = null,
    val otp: Otp? = null,
    
    val password: Password? = null,
) : FormData {
    data class Password(
        val value: SyncObfuscatedValue?,
        val idFromPasswordGenerator: String? = null
    )

    data class LinkedServices(
        val addedByUserApps: List<String> = emptyList(),
        val addedByDashlaneApps: List<String> = emptyList(),
        val addedByUserDomains: List<String> = emptyList(),
        val addedByDashlaneDomains: List<String> = emptyList(),
        val size: Int = addedByUserApps.size + addedByDashlaneApps.size + addedByUserDomains.size + addedByDashlaneDomains.size
    )
}

internal fun SummaryObject.Authentifiant.toCredentialFormData(
    availableSpaces: List<TeamSpace>,
    isEditable: Boolean?,
    isCopyActionAllowed: Boolean,
    canDelete: Boolean?,
    sharingCount: FormData.SharingCount?,
    teamSpace: TeamSpace?,
    isSharedWithLimitedRight: Boolean
) = Data(
    commonData = CommonData(
        id = this.id,
        name = this.title ?: "",
        isShared = this.isShared,
        isEditable = isEditable ?: false,
        isCopyActionAllowed = isCopyActionAllowed,
        canDelete = canDelete ?: false,
        sharingCount = sharingCount ?: FormData.SharingCount(),
        space = teamSpace,
        availableSpaces = availableSpaces, 
        created = this.creationDatetime,
        updated = this.userModificationDatetime,
        isSharedWithLimitedRight = isSharedWithLimitedRight,
        attachmentCount = attachmentsCount()
    ),
    formData = CredentialFormData(
        email = this.email,
        login = this.login,
        secondaryLogin = this.secondaryLogin,
        url = this.urlForUI(),
        note = this.note,
        linkedServices = this.getItemLinkedServices()
    )
)

fun SummaryObject.Authentifiant.getItemLinkedServices() =
    CredentialFormData.LinkedServices(
        linkedServices?.associatedAndroidApps
            ?.filter { it.linkSource == SummaryObject.LinkedServices.AssociatedAppsSource.USER }
            ?.mapNotNull { it.packageName }
            ?: emptyList(),
        linkedServices?.associatedAndroidApps
            ?.filter { it.linkSource == SummaryObject.LinkedServices.AssociatedAppsSource.DASHLANE }
            ?.mapNotNull { it.packageName }
            ?: emptyList(),
        linkedServices?.associatedDomains?.map { it.domain } ?: emptyList(),
        KnownLinkedDomains.getMatchingLinkedDomainSet(urlDomain)?.map { it.value } ?: emptyList()
    )