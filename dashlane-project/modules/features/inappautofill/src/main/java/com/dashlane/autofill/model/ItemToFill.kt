package com.dashlane.autofill.model

import android.os.Parcelable
import com.dashlane.hermes.generated.definitions.MatchType
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import java.time.Instant
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

sealed class ItemToFill : Parcelable {
    abstract val itemId: String
    abstract val matchType: MatchType?
    abstract val lastUsedDate: Instant?
}

@Parcelize
sealed class ToUnlockItemToFill<T : SyncObject> : ItemToFill(), Parcelable {
    @IgnoredOnParcel
    var vaultItem: VaultItem<T>? = null

    @IgnoredOnParcel
    var isSharedWithLimitedRight: Boolean = false
}

@Parcelize
data class AuthentifiantItemToFill(
    override val itemId: String,
    override val matchType: MatchType?,
    override val lastUsedDate: Instant?,
    val title: String?,
    val login: String?,
    val url: String,
    val oldPassword: SyncObfuscatedValue?
) : ToUnlockItemToFill<SyncObject.Authentifiant>(), Parcelable

@Parcelize
data class CreditCardItemToFill(
    override val itemId: String,
    override val matchType: MatchType?,
    override val lastUsedDate: Instant?,
    val name: String?,
    val bankName: String?,
    val cardNumberObfuscate: String,
    val zipCode: String?,
    val color: SyncObject.PaymentCreditCard.Color?,
) : ToUnlockItemToFill<SyncObject.PaymentCreditCard>(), Parcelable

@Parcelize
data class EmailItemToFill(
    override val itemId: String,
    override val matchType: MatchType?,
    override val lastUsedDate: Instant?,
    val name: String?,
    val email: String?
) : ItemToFill(), Parcelable

@Parcelize
data class OtpItemToFill(
    override val itemId: String = "empty",
    override val matchType: MatchType? = null,
    override val lastUsedDate: Instant? = null,
    val code: String
) : ToUnlockItemToFill<SyncObject>(), Parcelable