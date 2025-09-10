package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.DisableSetting
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.ItemType
import javax.annotation.processing.Generated

@Generated
data class AutofillSettingAnonymous(
    val disableSetting: DisableSetting,
    val domain: Domain? = null,
    val itemTypeList: List<ItemType>? = null,
    val isNativeApp: Boolean,
) : TrackingLog {
    override val name
        get() = "autofill_setting"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["disable_setting"] = disableSetting
        collector["domain"] = domain
        collector["item_type_list"] = itemTypeList
        collector["is_native_app"] = isNativeApp
    }
}