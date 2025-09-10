package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.ItemTypeCounts
import com.dashlane.hermes.generated.definitions.Scope
import javax.annotation.processing.Generated

@Generated
data class VaultReport(
    val itemsSharedCount: Int? = null,
    val passkeys: ItemTypeCounts? = null,
    val passwordsSafeCount: Int,
    val securityAlertsActiveCount: Int? = null,
    val collectionsPerItemAverageCount: Int? = null,
    val payments: ItemTypeCounts,
    val itemsPerCollectionAverageCount: Int? = null,
    val darkWebAlertsCount: Int? = null,
    val passwordsReusedCompatibleWithPasswordChangerCount: Int? = null,
    val passwordsExcludedCount: Int,
    val personalInfo: ItemTypeCounts,
    val passwords: ItemTypeCounts,
    val passwordsWeakCompatibleWithPasswordChangerCount: Int? = null,
    val passwordChangerCompatibleCredentialsCount: Int? = null,
    val secureNotes: ItemTypeCounts,
    val scope: Scope,
    val passwordsWeakCount: Int,
    val securityScore: Int? = null,
    val passwordsTotalCount: Int? = null,
    val domainsWithoutAutofillCount: Int? = null,
    val darkWebAlertsActiveCount: Int? = null,
    val passwordsReusedCount: Int,
    val securityAlertsCount: Int? = null,
    val itemsTotalCount: Int? = null,
    val secrets: ItemTypeCounts? = null,
    val passwordsCompromisedThroughDarkWebCount: Int? = null,
    val passwordsProtectedWithMasterPasswordCount: Int? = null,
    val passwordsWithAutologinDisabledCount: Int? = null,
    val passwordsCompromisedCompatibleWithPasswordChangerCount: Int? = null,
    val collectionsSharedCount: Int? = null,
    val ids: ItemTypeCounts,
    val passwordsWithOtpCount: Int,
    val collectionsTotalCount: Int? = null,
    val passwordsCompromisedCount: Int,
) : TrackingLog {
    override val name
        get() = "vault_report"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["items_shared_count"] = itemsSharedCount
        collector["passkeys"] = passkeys
        collector["passwords_safe_count"] = passwordsSafeCount
        collector["security_alerts_active_count"] = securityAlertsActiveCount
        collector["collections_per_item_average_count"] = collectionsPerItemAverageCount
        collector["payments"] = payments
        collector["items_per_collection_average_count"] = itemsPerCollectionAverageCount
        collector["dark_web_alerts_count"] = darkWebAlertsCount
        collector["passwords_reused_compatible_with_password_changer_count"] = passwordsReusedCompatibleWithPasswordChangerCount
        collector["passwords_excluded_count"] = passwordsExcludedCount
        collector["personal_info"] = personalInfo
        collector["passwords"] = passwords
        collector["passwords_weak_compatible_with_password_changer_count"] = passwordsWeakCompatibleWithPasswordChangerCount
        collector["password_changer_compatible_credentials_count"] = passwordChangerCompatibleCredentialsCount
        collector["secure_notes"] = secureNotes
        collector["scope"] = scope
        collector["passwords_weak_count"] = passwordsWeakCount
        collector["security_score"] = securityScore
        collector["passwords_total_count"] = passwordsTotalCount
        collector["domains_without_autofill_count"] = domainsWithoutAutofillCount
        collector["dark_web_alerts_active_count"] = darkWebAlertsActiveCount
        collector["passwords_reused_count"] = passwordsReusedCount
        collector["security_alerts_count"] = securityAlertsCount
        collector["items_total_count"] = itemsTotalCount
        collector["secrets"] = secrets
        collector["passwords_compromised_through_dark_web_count"] = passwordsCompromisedThroughDarkWebCount
        collector["passwords_protected_with_master_password_count"] = passwordsProtectedWithMasterPasswordCount
        collector["passwords_with_autologin_disabled_count"] = passwordsWithAutologinDisabledCount
        collector["passwords_compromised_compatible_with_password_changer_count"] = passwordsCompromisedCompatibleWithPasswordChangerCount
        collector["collections_shared_count"] = collectionsSharedCount
        collector["ids"] = ids
        collector["passwords_with_otp_count"] = passwordsWithOtpCount
        collector["collections_total_count"] = collectionsTotalCount
        collector["passwords_compromised_count"] = passwordsCompromisedCount
    }
}