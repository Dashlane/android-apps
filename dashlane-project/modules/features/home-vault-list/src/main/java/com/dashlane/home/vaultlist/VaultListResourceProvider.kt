package com.dashlane.home.vaultlist

import android.content.res.Resources
import com.dashlane.vault.textfactory.list.DataIdentifierTypeTextFactory
import com.dashlane.vaultlist.R
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

data class VaultListResourceProvider @Inject constructor(
    private val resources: Resources,
) {
    val vaultListEmptyAllTitle
        get() = resources.getString(R.string.empty_screen_vault_allitems_line1)

    val vaultListEmptyAllDescription
        get() = resources.getString(R.string.empty_screen_vault_allitems_line2)

    val vaultListEmptyPasswordTitle
        get() = resources.getString(R.string.import_methods_empty_state_title)

    val vaultListEmptyPasswordDescription
        get() = resources.getString(R.string.import_methods_empty_state_description)

    val vaultListEmptySecureNoteTitle
        get() = resources.getString(R.string.empty_screen_securenotes_line1)

    val vaultListEmptySecureNoteDescription
        get() = resources.getString(R.string.empty_screen_securenotes_line2)

    val vaultListEmptyPaymentTitle
        get() = resources.getString(R.string.empty_screen_payments_line1)

    val vaultListEmptyPaymentDescription
        get() = resources.getString(R.string.empty_screen_payments_line2)

    val vaultListEmptyPersonalInfoTitle
        get() = resources.getString(R.string.empty_screen_personalinfo_line1)

    val vaultListEmptyPersonalInfoDescription
        get() = resources.getString(R.string.empty_screen_personalinfo_line2)

    val vaultListEmptyIdTitle
        get() = resources.getString(R.string.empty_screen_ids_line1)

    val vaultListEmptyIdDescription
        get() = resources.getString(R.string.empty_screen_ids_line2)

    val vaultListEmptySecretTitle
        get() = resources.getString(R.string.empty_screen_secret_line1)

    val vaultListEmptySecretDescription
        get() = resources.getString(R.string.empty_screen_secret_line2)

    fun getCategoryHeader(syncObjectType: SyncObjectType): String {
        return resources.getString(DataIdentifierTypeTextFactory.getStringResId(syncObjectType))
    }

    val vaultListMostRecentHeader: String
        get() = resources.getString(R.string.vault_header_most_recent)

    val vaultListSuggestedHeader: String
        get() = resources.getString(R.string.vault_header_suggested)
}