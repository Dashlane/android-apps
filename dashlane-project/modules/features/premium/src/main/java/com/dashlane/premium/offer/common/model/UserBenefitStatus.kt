package com.dashlane.premium.offer.common.model

data class UserBenefitStatus(val type: Type, val renewPeriodicity: RenewPeriodicity) {
    sealed class Type {
        object Legacy : Type()
        object Free : Type()
        object Trial : Type()
        object AdvancedIndividual : Type()
        object PremiumIndividual : Type()
        object PremiumPlusIndividual : Type()
        data class Family(val isAdmin: Boolean) : Type()
        data class FamilyPlus(val isAdmin: Boolean) : Type()
        object Unknown : Type()
    }

    enum class RenewPeriodicity { UNKNOWN, MONTHLY, YEARLY, NONE }
}