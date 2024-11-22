package com.dashlane.vault.util

import com.dashlane.regioninformation.banks.BankRepository
import com.dashlane.util.BuildConfig
import com.dashlane.vault.model.BankConfiguration
import com.dashlane.vault.model.CreditCardBank
import com.dashlane.vault.model.CreditCardBank.Companion.US_NO_TYPE
import com.dashlane.xml.domain.utils.Country
import javax.inject.Inject

class BankDataProvider @Inject constructor(
    private val bankRepository: BankRepository,
) {
    private val banks: List<BankConfiguration> by lazy {
        val bankRegions = bankRepository.getBankRegions()
        bankRegions.flatMap { (regionCode, banks) ->
            val country = Country.forIsoCodeOrNull(regionCode)
            banks.map { (bankCode, name) ->
                BankConfiguration("$regionCode-$bankCode", name, country)
            }
        }
    }

    fun getBankConfiguration(bankDescriptor: String?): BankConfiguration {
        val correctedBankDescriptor = transformLegacyDescriptor(bankDescriptor)
        return banks.firstOrNull { it.bankDescriptor == correctedBankDescriptor }
            ?: DEFAULT_BANK.takeUnless { BuildConfig.DEBUG && correctedBankDescriptor!!.isNotEmpty() }
            ?: throw (IllegalArgumentException("Unsupported bank: $correctedBankDescriptor"))
    }

    fun isCountrySupported(country: Country): Boolean =
        getBankDescriptorsForCountry(country).isNotEmpty()

    fun getBankDescriptorsForCountry(country: Country): List<String> =
        banks.filter { it.country == country }.map { it.bankDescriptor }

    
    private fun transformLegacyDescriptor(bankDescriptor: String?): String? {
        return CreditCardBank.BankDescriptor.get(bankDescriptor)
    }

    companion object {
        val DEFAULT_BANK = BankConfiguration(
            US_NO_TYPE,
            "Other",
            Country.UnitedStates
        )
    }
}