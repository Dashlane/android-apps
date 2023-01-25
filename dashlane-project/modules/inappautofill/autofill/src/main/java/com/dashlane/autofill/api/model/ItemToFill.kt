package com.dashlane.autofill.api.model

import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import java.time.Instant

sealed class ItemToFill {
    abstract val matchType: MatchType?
    abstract val lastUsedDate: Instant?

    abstract fun getItemId(): String?
}

data class EmailItemToFill(
    val primaryItem: SummaryObject.Email,
    override val matchType: MatchType? = null,
    override val lastUsedDate: Instant? = null
) : ItemToFill() {
    override fun getItemId(): String = primaryItem.id
}

data class AuthentifiantSummaryItemToFill(
    val primaryItem: SummaryObject.Authentifiant,
    override val matchType: MatchType? = null,
    override val lastUsedDate: Instant? = null
) : ItemToFill() {
    override fun getItemId(): String = primaryItem.id
}

data class AuthentifiantItemToFill(
    val primaryItem: VaultItem<SyncObject.Authentifiant>,
    val oldItem: SyncObject.Authentifiant? = null,
    override val matchType: MatchType? = null,
    override val lastUsedDate: Instant? = null
) : ItemToFill() {
    override fun getItemId(): String? = primaryItem.syncObject.id
}

data class CreditCardItemToFill(
    val primaryItem: VaultItem<SyncObject.PaymentCreditCard>,
    val optional: SummaryObject.Address? = null,
    override val matchType: MatchType? = null,
    override val lastUsedDate: Instant? = null
) : ItemToFill() {
    override fun getItemId(): String? = primaryItem.syncObject.id
}

data class CreditCardSummaryItemToFill(
    val primaryItem: SummaryObject.PaymentCreditCard,
    val optional: SummaryObject.Address? = null,
    override val matchType: MatchType? = null,
    override val lastUsedDate: Instant? = null
) : ItemToFill() {
    override fun getItemId(): String = primaryItem.id
}

data class TextItemToFill(
    val value: String,
    override val matchType: MatchType? = null,
    override val lastUsedDate: Instant? = null
) : ItemToFill() {
    override fun getItemId(): String? = null
}