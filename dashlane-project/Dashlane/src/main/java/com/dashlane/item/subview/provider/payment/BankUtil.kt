package com.dashlane.item.subview.provider.payment

import com.dashlane.util.BankDataProvider
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.utils.Country

fun BankDataProvider.getBankName(bankDescriptor: String, defaultName: String): String {
    return if (bankDescriptor.endsWith("-NO_TYPE")) {
        defaultName
    } else {
        getBankConfiguration(bankDescriptor).displayName
    }
}

fun BankDataProvider.getCreditCardBankListCurrentCountry(
    item: VaultItem<*>,
    defaultName: String
): List<Pair<String, String?>> {
    
    return getCreditCardBankList(
        country = item.syncObject.localeFormat ?: Country.UnitedStates,
        defaultName = defaultName
    )
}

fun BankDataProvider.getCreditCardBankList(
    country: Country,
    defaultName: String
): List<Pair<String, String?>> {
    val bankList = arrayListOf<Pair<String, String?>>()

    val bankDescriptorList = getBankDescriptorsForCountry(country)
    bankDescriptorList.forEach {
        bankList.add(Pair(getBankName(it, defaultName), it))
    }
    return bankList
}