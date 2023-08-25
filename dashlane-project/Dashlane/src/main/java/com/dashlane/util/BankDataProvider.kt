package com.dashlane.util

import com.dashlane.BuildConfig
import com.dashlane.displayconfiguration.BankConfiguration
import com.dashlane.regioninformation.banks.BankRepositoryImpl
import com.dashlane.vault.model.CreditCardBank
import com.dashlane.vault.model.CreditCardBank.Companion.US_NO_TYPE
import com.dashlane.xml.domain.utils.Country

class BankDataProvider(private val banks: List<BankConfiguration>) {

    fun getBankConfiguration(bankDescriptor: String?): BankConfiguration {
        val correctedBankDescriptor = transformLegacyDescriptor(bankDescriptor)
        return banks.firstOrNull { it.bankDescriptor == correctedBankDescriptor }
            ?: if (BuildConfig.DEBUG && !correctedBankDescriptor!!.isEmpty()) {
                throw IllegalArgumentException("Unsupported bank: $correctedBankDescriptor")
            } else {
                DEFAULT_BANK
            }
    }

    fun isCountrySupported(country: Country): Boolean =
        getBankDescriptorsForCountry(country).isNotEmpty()

    fun getBankDescriptorsForCountry(country: Country): List<String> =
        banks.filter { it.country == country }.map { it.bankDescriptor }

    companion object {

        private val DEFAULT_BANK = BankConfiguration(
            US_NO_TYPE,
            "Other",
            Country.UnitedStates
        )

        val instance: BankDataProvider by lazy {
            BankDataProvider(getBankConfigurations())
        }

        
        private fun transformLegacyDescriptor(bankDescriptor: String?): String? {
            return CreditCardBank.BankDescriptor.get(bankDescriptor)
        }

        private fun getBankConfigurations(): List<BankConfiguration> {
            val bankRepository = BankRepositoryImpl()
            val bankRegions = bankRepository.getBankRegions()
            return bankRegions.flatMap { (regionCode, banks) ->
                val country = Country.forIsoCodeOrNull(regionCode)
                banks.map { (bankCode, name) ->
                    BankConfiguration("$regionCode-$bankCode", name, country)
                }
            }
        }
    }
}