package com.dashlane.item.v3.itemtype.creditcard.loader

import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.item.v3.itemtype.creditcard.CreditCardFormData
import com.dashlane.item.v3.viewmodels.ItemEditState
import com.dashlane.server.api.endpoints.teams.ActivityLog
import com.dashlane.server.api.endpoints.teams.Properties
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.VaultItemLogger
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class CreditCardSensitiveFieldLoader @Inject constructor(
    private val vaultDataQuery: VaultDataQuery,
    private val vaultItemLogger: VaultItemLogger,
) {
    suspend fun loadCardNumber(state: ItemEditState<CreditCardFormData>): String? {
        val id = state.itemId ?: return null
        val currentFormData = state.datas?.current ?: return null

        return if (currentFormData.formData.cardNumber != null) {
            currentFormData.formData.cardNumber
        } else {
            val vaultItem = loadSyncObject(id) ?: return null

            vaultItem.syncObject.cardNumber.toString()
                .takeIf { it.isNotSemanticallyNull() }
                ?.also {
                    vaultItemLogger.activityLogRevealCreditCardField(
                        item = vaultItem,
                        propertyField = Properties.Field.NUMBER,
                        logType = ActivityLog.LogType.USER_REVEALED_CREDIT_CARD_FIELD,
                    )
                    vaultItemLogger.logRevealField(
                        field = Field.CARD_NUMBER,
                        itemId = vaultItem.uid,
                        itemType = ItemType.CREDIT_CARD,
                        domain = null,
                    )
                }
        }
    }

    suspend fun loadSecurityCode(state: ItemEditState<CreditCardFormData>): String? {
        val id = state.itemId ?: return null
        val currentFormData = state.datas?.current ?: return null

        return if (currentFormData.formData.securityCode != null) {
            currentFormData.formData.securityCode
        } else {
            val vaultItem = loadSyncObject(id) ?: return null

            vaultItem.syncObject.securityCode.toString()
                .takeIf { it.isNotSemanticallyNull() }
                ?.also {
                    vaultItemLogger.activityLogRevealCreditCardField(
                        item = vaultItem,
                        propertyField = Properties.Field.CVV,
                        logType = ActivityLog.LogType.USER_REVEALED_CREDIT_CARD_FIELD,
                    )
                    vaultItemLogger.logRevealField(
                        field = Field.SECURITY_CODE,
                        itemId = vaultItem.uid,
                        itemType = ItemType.CREDIT_CARD,
                        domain = null,
                    )
                }
        }
    }

    suspend fun loadNote(state: ItemEditState<CreditCardFormData>): String? {
        val id = state.itemId ?: return null
        val currentFormData = state.datas?.current ?: return null

        return if (currentFormData.formData.note != null) {
            currentFormData.formData.note
        } else {
            val vaultItem = loadSyncObject(id) ?: return null

            vaultItem.syncObject.cCNote.toString()
                .takeIf { it.isNotSemanticallyNull() }
                ?.also {
                    vaultItemLogger.logRevealField(
                        field = Field.NOTE,
                        itemId = vaultItem.uid,
                        itemType = ItemType.CREDIT_CARD,
                        domain = null,
                    )
                }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun loadSyncObject(id: String): VaultItem<SyncObject.PaymentCreditCard>? =
        vaultDataQuery.query(vaultFilter { specificUid(id) })
            .takeIf { it is VaultItem<*> && it.syncObject is SyncObject.PaymentCreditCard }
            ?.let { it as? VaultItem<SyncObject.PaymentCreditCard> }
}