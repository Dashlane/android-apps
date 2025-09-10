package com.dashlane.item.v3.itemtype.creditcard

import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.item.v3.util.ItemEditValueUpdateItemManager
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.obfuscated.matchesNullAsEmpty
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import java.time.Month
import java.time.Year
import javax.inject.Inject

class ItemEditValueUpdateItemCreditCardManager @Inject constructor() :
    ItemEditValueUpdateItemManager<CreditCardFormData, SyncObject.PaymentCreditCard> {
        private val editedFields = mutableSetOf<Field>()

        override fun updateWithData(
            data: Data<CreditCardFormData>,
            initialVaultItem: VaultItem<SyncObject.PaymentCreditCard>,
        ): Pair<VaultItem<SyncObject.PaymentCreditCard>, Set<Field>> {
            editedFields.clear()
            val updatedItem = initialVaultItem.copy(
                syncObject = initialVaultItem.syncObject
                    .copyForUpdatedTeamspace(data.commonData.space ?: TeamSpace.Personal)
                    .copyForUpdatedHolder(data.formData.cardHolder)
                    .copyForUpdatedNumber(data.formData.cardNumber)
                    .copyForUpdatedSecurityCode(data.formData.securityCode)
                    .copyForUpdatedExpirationDate(
                        data.formData.expirationYear?.let { Year.of(it) },
                        data.formData.expirationMonth?.let { Month.of(it) },
                    )
                    .copyForUpdatedNote(data.formData.note)
                    .copyForUpdatedColor(data.formData.colorId)
                    .copyForUpdatedItemName(data.commonData.name),
            )

            return updatedItem to editedFields
        }

        private fun SyncObject.PaymentCreditCard.copyForUpdatedHolder(value: String?): SyncObject.PaymentCreditCard {
            return if (value == this.ownerName) {
                this
            } else {
                editedFields += Field.OWNER_NAME
                this.copy { ownerName = value }
            }
        }

        private fun SyncObject.PaymentCreditCard.copyForUpdatedNumber(value: String?): SyncObject.PaymentCreditCard =
            if (this.cardNumber.matchesNullAsEmpty(value)) {
                this
            } else {
                editedFields += Field.CARD_NUMBER
                this.copy {
                    cardNumber = value.toSyncObfuscatedValue()
                }
            }

        private fun SyncObject.PaymentCreditCard.copyForUpdatedSecurityCode(value: String?): SyncObject.PaymentCreditCard =
            if (this.securityCode.matchesNullAsEmpty(value)) {
                this
            } else {
                editedFields += Field.SECURITY_CODE
                this.copy {
                    securityCode = value.toSyncObfuscatedValue()
                }
            }

        private fun SyncObject.PaymentCreditCard.copyForUpdatedExpirationDate(
            year: Year?,
            month: Month?,
        ): SyncObject.PaymentCreditCard =
            if (year == null || month == null) {
                this
            } else if (this.expireYear == year && this.expireMonth == month) {
                this
            } else {
                editedFields += Field.EXPIRE_DATE
                this.copy {
                    expireYear = year
                    expireMonth = month
                }
            }

        private fun SyncObject.PaymentCreditCard.copyForUpdatedNote(value: String?): SyncObject.PaymentCreditCard =
            if (this.cCNote.matchesNullAsEmpty(value)) {
                this
            } else {
                editedFields += Field.NOTE
                this.copy {
                    cCNote = value.toSyncObfuscatedValue()
                }
            }

        private fun SyncObject.PaymentCreditCard.copyForUpdatedItemName(value: String?): SyncObject.PaymentCreditCard =
            if (this.name.orEmpty() == value.orEmpty()) {
                this
            } else {
                editedFields += Field.TITLE
                this.copy {
                    name = value
                }
            }

        private fun SyncObject.PaymentCreditCard.copyForUpdatedColor(value: String?): SyncObject.PaymentCreditCard {
            val color = SyncObject.PaymentCreditCard.Color.entries.firstOrNull { it.value == value }
            return if (this.color == color) {
                this
            } else {
                editedFields += Field.COLOR
                this.copy {
                    this.color = color
                }
            }
        }

        private fun SyncObject.PaymentCreditCard.copyForUpdatedTeamspace(value: TeamSpace): SyncObject.PaymentCreditCard {
            val spaceId = this.spaceId ?: TeamSpace.Personal.teamId
            return if (value.teamId == spaceId) {
                this
            } else {
                this.copy { this.spaceId = value.teamId }
            }
        }
    }