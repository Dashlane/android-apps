package com.dashlane.followupnotification.domain

import android.content.Context
import com.dashlane.followupnotification.services.FollowUpNotificationDynamicData
import com.dashlane.followupnotification.services.FollowUpNotificationsStrings
import com.dashlane.followupnotification.services.VaultItemContentService
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.urlForUsageLog
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.SharingStateChecker
import com.dashlane.vault.util.isProtected
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CreateFollowUpNotificationImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val followUpNotificationsStrings: FollowUpNotificationsStrings,
    private val followUpNotificationDynamicData: FollowUpNotificationDynamicData,
    private val vaultItemContentDisplayService: VaultItemContentService,
    private val vaultDataQuery: VaultDataQuery,
) : CreateFollowUpNotification {

    override fun createFollowUpNotification(
        summaryObject: SummaryObject,
        copyField: CopyField?
    ): FollowUpNotification? {
        val vaultItem = vaultDataQuery.query(vaultFilter { specificUid(summaryObject.id) })
        if (!isFollowUpNotificationNeeded(summaryObject, copyField)) return null
        val followUpNotificationsType =
            summaryObject.getFollowUpType(context, vaultItem?.syncObject?.localeFormat) ?: return null
        val copyFields = if (credentialHasBothLoginAndEmail(summaryObject)) {
            followUpNotificationsType.copyField.toList().minus(CopyField.Email)
        } else {
            followUpNotificationsType.copyField.toList()
        }.filterFieldsWithLimitedPermissions(summaryObject)

        val followUpNotificationFields = copyFields.mapNotNull {
            if (it == CopyField.OtpCode && !(summaryObject as SummaryObject.Authentifiant).hasOtpUrl) {
                return@mapNotNull null
            }
            val contentDisplay = vaultItemContentDisplayService.getContent(summaryObject, it)
                .takeIf { content: FollowUpNotification.FieldContent? -> content?.displayValue.isNotSemanticallyNull() }
                ?: return@mapNotNull null
            val label = followUpNotificationsStrings.getFieldLabel(it) ?: return@mapNotNull null
            FollowUpNotification.Field(it.name, label, contentDisplay)
        }.takeIf {
            
            it.size > 1
        } ?: return null

        val followUpNotificationName = summaryObject.getItemName()
            ?: followUpNotificationsStrings.getFollowUpNotificationsTypesLabels(followUpNotificationsType)
        val followUpNotificationId = followUpNotificationDynamicData.generateRandomUUIDString()

        return FollowUpNotification(
            id = followUpNotificationId,
            vaultItemId = summaryObject.id,
            type = followUpNotificationsType,
            name = followUpNotificationName,
            fields = followUpNotificationFields,
            isItemProtected = summaryObject.isProtected,
            itemDomain = (summaryObject as? SummaryObject.Authentifiant)?.urlForUsageLog
        )
    }

    private fun isFollowUpNotificationNeeded(summaryObject: SummaryObject, copyField: CopyField?): Boolean {
        if (summaryObject is SummaryObject.Authentifiant) {
            
            if (copyField == CopyField.Password && !summaryObject.hasOtpUrl) {
                return false
            }

            
            if (copyField == CopyField.OtpCode) {
                return false
            }
        }
        return true
    }

    override fun refreshExistingNotification(currentNotification: FollowUpNotification): FollowUpNotification {
        return FollowUpNotification(
            followUpNotificationDynamicData.generateRandomUUIDString(),
            currentNotification.vaultItemId,
            currentNotification.type,
            currentNotification.name,
            currentNotification.fields,
            currentNotification.isItemProtected,
            currentNotification.itemDomain
        )
    }

    private fun credentialHasBothLoginAndEmail(summaryObject: SummaryObject): Boolean =
        vaultItemContentDisplayService.getContent(
            summaryObject,
            CopyField.Login
        )?.displayValue.isNotSemanticallyNull() &&
            vaultItemContentDisplayService.getContent(
                summaryObject,
                CopyField.Email
            )?.displayValue.isNotSemanticallyNull()

    private fun SummaryObject.getItemName(): String? = when (this) {
        is SummaryObject.Authentifiant -> this.title
        is SummaryObject.Address -> this.addressName
        is SummaryObject.BankStatement -> this.bankAccountName
        is SummaryObject.Company -> this.name
        is SummaryObject.Email -> this.emailName
        is SummaryObject.PaymentCreditCard -> this.name
        is SummaryObject.PaymentPaypal -> this.name
        is SummaryObject.Collection -> name
        is SummaryObject.AuthCategory,
        is SummaryObject.SecureNoteCategory,
        is SummaryObject.SecureNote,
        is SummaryObject.DriverLicence,
        is SummaryObject.FiscalStatement,
        is SummaryObject.IdCard,
        is SummaryObject.Identity,
        is SummaryObject.Passport,
        is SummaryObject.PersonalWebsite,
        is SummaryObject.Phone,
        is SummaryObject.SocialSecurityStatement,
        is SummaryObject.SecureFileInfo,
        is SummaryObject.SecurityBreach,
        is SummaryObject.GeneratedPassword,
        is SummaryObject.DataChangeHistory,
        is SummaryObject.Passkey -> null
    }.takeIf { it.isNotSemanticallyNull() }

    private fun List<CopyField>.filterFieldsWithLimitedPermissions(summaryObject: SummaryObject): List<CopyField> =
        this.filterNot { copyField ->
            copyField.isSharingProtected &&
                SharingStateChecker.hasLimitedSharingRights(summaryObject)
        }
}
