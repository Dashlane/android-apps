package com.dashlane.item.v3.itemtype.creditcard.builder

import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.item.nfc.NfcHelper
import com.dashlane.item.v3.data.CommonData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.itemtype.creditcard.CreditCardFormData
import com.dashlane.item.v3.repositories.ItemEditRepository
import com.dashlane.item.v3.util.Field
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.expireDate
import com.dashlane.vault.model.getColorResource
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class CreditCardFormBuilder @Inject constructor(
    private val frozenStateManager: FrozenStateManager,
    private val creditCardTypeFormatBuilder: CreditCardTypeFormatBuilder,
    private val creditCardHiddenFieldsBuilder: CreditCardHiddenFieldsBuilder,
    private val teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    private val itemEditRepository: ItemEditRepository,
    private val nfcHelper: NfcHelper,
) : FormData.Builder<CreditCardFormData>() {
    override suspend fun build(
        initialSummaryObject: SummaryObject,
        isNew: Boolean,
    ): Data<CreditCardFormData> {
        val summaryObject = (initialSummaryObject as SummaryObject.PaymentCreditCard)
        isCopyActionAllowed = !frozenStateManager.isAccountFrozen

        teamSpace = itemEditRepository.getTeamspace(initialSummaryObject.spaceId)
        availableSpaces = teamSpaceAccessorProvider.get()
            ?.availableSpaces?.minus(TeamSpace.Combined) ?: emptyList()
        if (isNew && availableSpaces.isNotEmpty()) {
            
            teamSpace = availableSpaces.firstOrNull {
                it != TeamSpace.Personal
            } ?: TeamSpace.Personal
        }

        return with(summaryObject) {
            Data(
                commonData = buildCreditCardCommonData(isNew = isNew),
                formData = buildCreditCardFormData(),
            )
        }
    }

    private fun SummaryObject.PaymentCreditCard.buildCreditCardCommonData(isNew: Boolean): CommonData {
        return CommonData(
            id = this.id,
            name = this.name.takeIf { it.isNotSemanticallyNull() },
            isCopyActionAllowed = isCopyActionAllowed,
            created = this.creationDatetime,
            updated = this.userModificationDatetime,
            canDelete = isNew.not(),
            isEditable = true,
            space = teamSpace,
            availableSpaces = availableSpaces,
        )
    }

    override fun buildHiddenFields(data: CreditCardFormData, commonData: CommonData): Set<Field> =
        creditCardHiddenFieldsBuilder(data = data, commonData = commonData)

    private fun SummaryObject.PaymentCreditCard.buildCreditCardFormData(): CreditCardFormData {
        val ownerName = ownerName
            .takeIf { it.isNotSemanticallyNull() }
        val color = this.color?.takeUnless {
            it == SyncObject.PaymentCreditCard.Color.NO_TYPE
        } ?: SyncObject.PaymentCreditCard.Color.BLUE_1

        return CreditCardFormData(
            cardHolder = ownerName,
            cardNumberFormat = creditCardTypeFormatBuilder.fromType(
                type = creditCardTypeName,
                length = cardNumberObfuscate?.length ?: 0,
            ),
            cardNumberLength = cardNumberObfuscate?.length ?: 0,
            isCardNumberEmpty = this.isCardNumberEmpty,
            isSecurityCodeEmpty = this.isSecurityCodeEmpty,
            expirationYear = this.expireDate?.year,
            expirationMonth = this.expireDate?.month?.value,
            colorId = color.value,
            color = color.getColorResource(),
            isNoteEmpty = this.isNoteEmpty,
            isNfcAvailable = nfcHelper.isNfcAvailable(),
        )
    }
}