package com.dashlane.vault.model

class CreditCardBank(bankDescriptor: String?) {

    val bankDescriptor = BankDescriptor.get(bankDescriptor?.takeIf { it.split('-').size == 2 }) ?: ""

    object BankDescriptor {

        
        private val LEGACY_DESCRIPTOR = mapOf(US_AMERICAN_EXPRESS to US_AMERICANEXPRESS)

        @JvmStatic
        fun get(label: String?): String? {
            return LEGACY_DESCRIPTOR[label] ?: label
        }
    }

    companion object {
        const val US_NO_TYPE = "US-NO_TYPE"
        const val US_AMERICAN_EXPRESS = "US-AMERICAN_EXPRESS"
        const val US_AMERICANEXPRESS = "US-AMERICANEXPRESS"
    }
}
