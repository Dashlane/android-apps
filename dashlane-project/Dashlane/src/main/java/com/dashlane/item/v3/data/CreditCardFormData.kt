package com.dashlane.item.v3.data

import com.dashlane.teamspaces.model.TeamSpace
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import java.time.Instant

data class CreditCardFormData(
    override val id: String = "",
    override val name: String = "",
    override val isShared: Boolean = false,
    override val isEditable: Boolean = true,
    override val isCopyActionAllowed: Boolean = true,
    override val canDelete: Boolean = false,
    override val sharingCount: SharingCount = SharingCount(),
    override val collections: List<CollectionData> = emptyList(),
    override val created: Instant? = null,
    override val updated: Instant? = null,
    override val space: TeamSpace? = null,
    override val availableSpaces: List<TeamSpace> = emptyList(),
    override val isForcedSpace: Boolean = false,
    val lastDigits: String? = null,
    val color: SyncObject.PaymentCreditCard.Color? = null
) : FormData()

internal fun SummaryObject.PaymentCreditCard.toCreditCardFormData(
    isCopyActionAllowed: Boolean,
) = CreditCardFormData(
    id = this.id,
    name = this.name ?: "",
    isShared = this.isShared,
    isCopyActionAllowed = isCopyActionAllowed,
    created = this.creationDatetime,
    updated = this.userModificationDatetime,
    lastDigits = this.cardNumberLastFourDigits,
    color = this.color
)