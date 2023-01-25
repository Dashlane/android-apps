package com.dashlane.item.logger

import com.dashlane.inapplogin.UsageLogCode35Action
import com.dashlane.item.subview.edit.ItemEditSpaceSubView
import com.dashlane.session.BySessionRepository
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.useractivity.log.usage.UsageLogCode11
import com.dashlane.useractivity.log.usage.UsageLogCode35
import com.dashlane.useractivity.log.usage.UsageLogCode68
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.usageLogCode68Data2
import com.dashlane.vault.util.TeamSpaceUtils
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

class CreditCardLogger(
    private val teamspaceAccessor: TeamspaceAccessor,
    dataCounter: DataCounter,
    sessionManager: SessionManager,
    bySessionUsageLogRepository: BySessionRepository<UsageLogRepository>
) : BaseLogger(teamspaceAccessor, dataCounter, sessionManager, bySessionUsageLogRepository) {

    var origin: String? = null

    @Suppress("UNCHECKED_CAST")
    override fun logItemAdded(
        vaultItem: VaultItem<*>,
        dataType: SyncObjectType,
        categorizationMethod: ItemEditSpaceSubView.CategorizationMethod?
    ) {
        super.logItemAdded(vaultItem, dataType, categorizationMethod)
        if (vaultItem.syncObject !is SyncObject.PaymentCreditCard) return
        sendUsageLog68(vaultItem as VaultItem<SyncObject.PaymentCreditCard>, UsageLogCode68.Action.ADD)
        origin = null
    }

    @Suppress("UNCHECKED_CAST")
    override fun logItemModified(
        vaultItem: VaultItem<*>,
        dataType: SyncObjectType,
        categorizationMethod: ItemEditSpaceSubView.CategorizationMethod?
    ) {
        super.logItemModified(vaultItem, dataType, categorizationMethod)
        if (vaultItem.syncObject !is SyncObject.PaymentCreditCard) return
        sendUsageLog68(vaultItem as VaultItem<SyncObject.PaymentCreditCard>, UsageLogCode68.Action.EDIT)
        origin = null
    }

    @Suppress("UNCHECKED_CAST")
    override fun logItemDeleted(vaultItem: VaultItem<*>, dataType: SyncObjectType) {
        super.logItemDeleted(vaultItem, dataType)
        if (vaultItem.syncObject !is SyncObject.PaymentCreditCard) return
        sendUsageLog68(vaultItem as VaultItem<SyncObject.PaymentCreditCard>, UsageLogCode68.Action.REMOVE)
        origin = null
    }

    fun logCopySecurityCode() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.PAYMENT_MEAN_CREDITCARD.code,
                action = UsageLogCode35Action.COPY_SECURITY_CODE
            )
        )
    }

    fun logCopyCardNumber() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.PAYMENT_MEAN_CREDITCARD.code,
                action = UsageLogCode35Action.COPY_NUMBER
            )
        )
    }

    fun logNfcDialogShown() {
        sendUsageLog75("scanPrompt", "display")
        origin = null
    }

    fun logNfcDialogDismissed() {
        sendUsageLog75("scanPrompt", "dismiss")
    }

    fun logNfcSuccessDialogShown() {
        sendUsageLog75("successScanPrompt", "display")
        origin = "nfcScan"
    }

    fun logNfcSuccessDialogClicked() {
        sendUsageLog75("successScanPrompt", "click")
    }

    private fun sendUsageLog75(subtype: String, action: String) {
        log(
            UsageLogCode75(
                type = "nfcCreditCardPopup",
                action = action,
                subtype = subtype
            )
        )
    }

    private fun sendUsageLog68(item: VaultItem<SyncObject.PaymentCreditCard>, action: UsageLogCode68.Action) {
        log(
            UsageLogCode68(
                spaceId = teamspaceAccessor.get(TeamSpaceUtils.getTeamSpaceId(item))?.anonTeamId,
                action = action,
                senderStr = if (origin == null) {
                    UsageLogCode68.Sender.MANUAL.code
                } else {
                    "${UsageLogCode68.Sender.MANUAL.code}_$origin"
                },
                identifier = item.anonymousId,
                data2 = item.syncObject.usageLogCode68Data2
            )
        )
    }

    fun logRevealCardNumber() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.PAYMENT_MEAN_CREDITCARD.code,
                action = UsageLogCode35Action.SHOW_NUMBER
            )
        )
    }

    fun logRevealCVV() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.PAYMENT_MEAN_CREDITCARD.code,
                action = UsageLogCode35Action.SHOW_SECURITY_CODE
            )
        )
    }

    fun logRevealNotes() {
        log(
            UsageLogCode35(
                type = UsageLogCode11.Type.PAYMENT_MEAN_CREDITCARD.code,
                action = UsageLogCode35Action.SHOW_NOTES
            )
        )
    }
}