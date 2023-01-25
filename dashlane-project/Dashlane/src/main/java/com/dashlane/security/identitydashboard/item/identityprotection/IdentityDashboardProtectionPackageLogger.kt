package com.dashlane.security.identitydashboard.item.identityprotection

interface IdentityDashboardProtectionPackageLogger {

    

    fun logOnActivePackageShow()

    

    fun logOnActiveSeeCreditView()

    

    fun logOnActiveProtectionLearnMore()

    

    fun logOnActiveRestorationLearnMore()

    companion object {
        const val IDENTITY_PROTECTION = "identity_protection"
    }
}