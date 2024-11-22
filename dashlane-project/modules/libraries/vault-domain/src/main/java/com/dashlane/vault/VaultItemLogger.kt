@file:Suppress("FunctionName")

package com.dashlane.vault

import com.dashlane.ext.application.TrustedBrowserApplication
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.Highlight
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.ItemTypeWithLink
import com.dashlane.hermes.generated.definitions.Space
import com.dashlane.hermes.generated.events.anonymous.CopyVaultItemFieldAnonymous
import com.dashlane.hermes.generated.events.anonymous.OpenExternalVaultItemLinkAnonymous
import com.dashlane.hermes.generated.events.anonymous.RestorePasswordAnonymous
import com.dashlane.hermes.generated.events.anonymous.RevealVaultItemFieldAnonymous
import com.dashlane.hermes.generated.events.anonymous.UpdateCredentialAnonymous
import com.dashlane.hermes.generated.events.user.CopyVaultItemField
import com.dashlane.hermes.generated.events.user.DownloadVaultItemAttachment
import com.dashlane.hermes.generated.events.user.OpenExternalVaultItemLink
import com.dashlane.hermes.generated.events.user.RestorePassword
import com.dashlane.hermes.generated.events.user.RevealVaultItemField
import com.dashlane.hermes.generated.events.user.SelectVaultItem
import com.dashlane.hermes.generated.events.user.UpdateVaultItem
import com.dashlane.hermes.generated.events.user.UpdateVaultItemAttachment
import com.dashlane.hermes.generated.events.user.ViewVaultItemAttachment
import com.dashlane.useractivity.hermes.TrackingLogUtils
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

class VaultItemLogger @Inject constructor(
    private val logRepository: LogRepository
) {
    fun logCopyField(
        highlight: Highlight? = null,
        field: Field,
        itemId: String,
        itemType: ItemType,
        totalCount: Int? = null,
        index: Double? = null,
        isProtected: Boolean,
        domain: String?
    ) {
        logRepository.queueEvent(
            CopyVaultItemField(
                highlight = highlight,
                index = index,
                field = field,
                itemId = ItemId(id = itemId),
                itemType = itemType,
                totalCount = totalCount,
                isProtected = isProtected
            )
        )
        domain?.let {
            logRepository.queueEvent(
                CopyVaultItemFieldAnonymous(
                    field = field,
                    itemType = itemType,
                    domain = Domain(Sha256Hash.of(domain), DomainType.WEB)
                )
            )
        }
    }

    fun logOpenExternalLink(
        itemId: String,
        packageName: String,
        url: String?
    ) {
        val isBrowser = TrustedBrowserApplication.getAppForPackage(packageName) != null
        val domain = if (isBrowser) {
            TrackingLogUtils.createWebDomainForLog(url.orEmpty())
        } else {
            TrackingLogUtils.createAppDomainForLog(packageName)
        }

        logRepository.queueEvent(
            OpenExternalVaultItemLink(
                domainType = domain.type,
                itemId = ItemId(id = itemId),
                itemType = ItemTypeWithLink.CREDENTIAL
            )
        )

        logRepository.queueEvent(
            OpenExternalVaultItemLinkAnonymous(
                itemType = ItemTypeWithLink.CREDENTIAL,
                domain = domain
            )
        )
    }

    fun logRevealField(
        field: Field,
        itemId: String,
        itemType: ItemType,
        domain: String?
    ) {
        logRepository.queueEvent(
            RevealVaultItemField(
                field = field,
                itemId = ItemId(id = itemId),
                itemType = itemType,
                isProtected = false
            )
        )
        domain?.let {
            logRepository.queueEvent(
                RevealVaultItemFieldAnonymous(
                    field = field,
                    itemType = itemType,
                    domain = Domain(Sha256Hash.of(domain), DomainType.WEB)
                )
            )
        }
    }

    fun logSelect(
        highlight: Highlight,
        itemId: String,
        index: Int?,
        itemType: ItemType,
        totalCount: Int?
    ) {
        logRepository.queueEvent(
            SelectVaultItem(
                highlight = highlight,
                index = index?.toDouble(),
                itemId = ItemId(id = itemId),
                itemType = itemType,
                totalCount = totalCount
            )
        )
    }

    fun logUpdate(
        action: Action,
        editedFields: List<Field>?,
        itemId: String,
        itemType: ItemType,
        space: Space,
        url: String?,
        addedWebsites: List<String>?,
        removedWebsites: List<String>?,
        removedApps: List<String>?,
        collectionCount: Int? = null 
    ) {
        logRepository.queueEvent(
            UpdateVaultItem(
                action = action,
                fieldsEdited = editedFields,
                itemId = ItemId(id = itemId),
                itemType = itemType,
                space = space,
                collectionCount = collectionCount
            )
        )

        if (itemType == ItemType.CREDENTIAL) {
            logRepository.queueEvent(
                UpdateCredentialAnonymous(
                    action = action,
                    domain = TrackingLogUtils.createWebDomainForLog(url.orEmpty()),
                    fieldList = editedFields,
                    space = space,
                    associatedWebsitesAddedList = addedWebsites?.map { Sha256Hash.of(it) },
                    associatedWebsitesRemovedList = removedWebsites?.map { Sha256Hash.of(it) },
                    associatedAppsRemovedList = removedApps?.map { Sha256Hash.of(it) },
                )
            )
        }
    }

    fun logAttachmentDownload(itemId: String, itemType: ItemType) {
        logRepository.queueEvent(
            DownloadVaultItemAttachment(
                itemId = ItemId(id = itemId),
                itemType = itemType
            )
        )
    }

    fun logAttachmentUpdate(action: Action, itemId: String, itemType: ItemType) {
        logRepository.queueEvent(
            UpdateVaultItemAttachment(
                attachmentAction = action,
                itemId = ItemId(id = itemId),
                itemType = itemType
            )
        )
    }

    fun logAttachmentView(itemId: String, itemType: ItemType) {
        logRepository.queueEvent(
            ViewVaultItemAttachment(
                itemId = ItemId(id = itemId),
                itemType = itemType
            )
        )
    }

    fun logPasswordRestored(itemId: String, url: String?) {
        logRepository.queueEvent(RestorePassword(ItemId(id = itemId)))
        logRepository.queueEvent(RestorePasswordAnonymous(TrackingLogUtils.createWebDomainForLog(url.orEmpty())))
    }
}

fun SyncObjectType.toItemTypeOrNull() = when (this) {
    SyncObjectType.ADDRESS -> ItemType.ADDRESS
    SyncObjectType.AUTHENTIFIANT, SyncObjectType.PASSKEY -> ItemType.CREDENTIAL
    SyncObjectType.BANK_STATEMENT -> ItemType.BANK_STATEMENT
    SyncObjectType.COMPANY -> ItemType.COMPANY
    SyncObjectType.DRIVER_LICENCE -> ItemType.DRIVER_LICENCE
    SyncObjectType.EMAIL -> ItemType.EMAIL
    SyncObjectType.FISCAL_STATEMENT -> ItemType.FISCAL_STATEMENT
    SyncObjectType.GENERATED_PASSWORD -> ItemType.GENERATED_PASSWORD
    SyncObjectType.ID_CARD -> ItemType.ID_CARD
    SyncObjectType.IDENTITY -> ItemType.IDENTITY
    SyncObjectType.PASSPORT -> ItemType.PASSPORT
    SyncObjectType.PAYMENT_CREDIT_CARD -> ItemType.CREDIT_CARD
    SyncObjectType.PERSONAL_WEBSITE -> ItemType.WEBSITE
    SyncObjectType.PHONE -> ItemType.PHONE
    SyncObjectType.SECURE_NOTE -> ItemType.SECURE_NOTE
    SyncObjectType.SECURITY_BREACH -> ItemType.SECURITY_BREACH
    SyncObjectType.SOCIAL_SECURITY_STATEMENT -> ItemType.SOCIAL_SECURITY
    SyncObjectType.SECRET -> ItemType.SECRET
    else -> null
}

fun SyncObjectType.toItemType() = toItemTypeOrNull() ?: throw IllegalStateException("No ItemType for $this")
