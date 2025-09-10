package com.dashlane.item.v3.util

import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.item.v3.data.CredentialFormData
import com.dashlane.item.v3.data.FormData
import com.dashlane.item.v3.data.SecretFormData
import com.dashlane.item.v3.data.SecureNoteFormData
import com.dashlane.item.v3.itemtype.creditcard.CreditCardFormData
import com.dashlane.item.v3.itemtype.creditcard.ItemEditValueUpdateItemCreditCardManager
import com.dashlane.item.v3.itemtype.wifi.ItemEditValueUpdateItemWiFiManager
import com.dashlane.item.v3.itemtype.wifi.WiFiFormData
import com.dashlane.item.v3.viewmodels.Data
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import javax.inject.Inject

class ItemEditValueUpdateManager @Inject constructor(
    private val authentifiantManager: ItemEditValueUpdateItemAuthentifiantManager,
    private val secureNoteManager: ItemEditValueUpdateItemSecureNoteManager,
    private val secretManager: ItemEditValueUpdateItemSecretManager,
    private val creditCardManager: ItemEditValueUpdateItemCreditCardManager,
    private val wiFiManager: ItemEditValueUpdateItemWiFiManager,
) {
    fun updateWithData(
        data: Data<out FormData>,
        initialVaultItem: VaultItem<SyncObject>,
    ): Pair<VaultItem<SyncObject>, Set<Field>> {
        @Suppress("UNCHECKED_CAST")
        return when (data.formData) {
            is CredentialFormData -> authentifiantManager.updateWithData(
                data = data as Data<CredentialFormData>,
                initialVaultItem = initialVaultItem as VaultItem<SyncObject.Authentifiant>,
            )

            is SecureNoteFormData -> secureNoteManager.updateWithData(
                data = data as Data<SecureNoteFormData>,
                initialVaultItem = initialVaultItem as VaultItem<SyncObject.SecureNote>,
            )

            is SecretFormData -> secretManager.updateWithData(
                data = data as Data<SecretFormData>,
                initialVaultItem = initialVaultItem as VaultItem<SyncObject.Secret>,
            )

            is CreditCardFormData -> creditCardManager.updateWithData(
                data = data as Data<CreditCardFormData>,
                initialVaultItem = initialVaultItem as VaultItem<SyncObject.PaymentCreditCard>,
            )

            is WiFiFormData -> wiFiManager.updateWithData(
                data = data as Data<WiFiFormData>,
                initialVaultItem = initialVaultItem as VaultItem<SyncObject.WiFi>,
            )
            else -> throw Error("Unsupported type: ${data.formData::class.simpleName}")
        }
    }
}