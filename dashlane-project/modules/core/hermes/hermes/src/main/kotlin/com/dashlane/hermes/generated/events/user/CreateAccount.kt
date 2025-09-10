package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.AccountCreationStatus
import com.dashlane.hermes.generated.definitions.Android
import com.dashlane.hermes.generated.definitions.Ios
import com.dashlane.hermes.generated.definitions.WebAccountCreation
import javax.annotation.processing.Generated

@Generated
data class CreateAccount(
    val isMarketingOptIn: Boolean,
    val androidMarketing: Android? = null,
    val webMarketing: WebAccountCreation? = null,
    val iosMarketing: Ios? = null,
    val status: AccountCreationStatus,
) : TrackingLog {
    override val name
        get() = "create_account"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = true

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_marketing_opt_in"] = isMarketingOptIn
        collector["android_marketing"] = androidMarketing
        collector["web_marketing"] = webMarketing
        collector["ios_marketing"] = iosMarketing
        collector["status"] = status
    }
}