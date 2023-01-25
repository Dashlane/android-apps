@file:JvmName("ItemWrapperProvider")

package com.dashlane.ui.activities.fragments.list.wrapper

import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary

fun VaultItem<*>.toItemWrapper(container: ItemListContext) =
    toSummary<SummaryObject>().toItemWrapper(container)



@Suppress("UNCHECKED_CAST")
fun SummaryObject.toItemWrapper(container: ItemListContext): VaultItemWrapper<out SummaryObject>? {
    return when (this) {
        is SummaryObject.Address,
        is SummaryObject.BankStatement,
        is SummaryObject.Company,
        is SummaryObject.FiscalStatement,
        is SummaryObject.Email,
        is SummaryObject.PaymentPaypal,
        is SummaryObject.PersonalWebsite,
        is SummaryObject.Phone,
        is SummaryObject.Identity,
        is SummaryObject.SocialSecurityStatement,
        is SummaryObject.SecureFileInfo,
        is SummaryObject.Authentifiant,
        is SummaryObject.DriverLicence,
        is SummaryObject.IdCard,
        is SummaryObject.Passport -> DefaultVaultItemWrapper(this, container)
        is SummaryObject.PaymentCreditCard -> PaymentCreditCardWrapper(this, container)
        is SummaryObject.SecureNote -> DefaultVaultItemWrapper(this, container)
        else -> {
            null
        }
    }
}