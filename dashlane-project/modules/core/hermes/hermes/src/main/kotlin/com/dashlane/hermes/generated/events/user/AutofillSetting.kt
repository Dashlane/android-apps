package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.DisableSetting
import com.dashlane.hermes.generated.definitions.ItemType
import javax.annotation.processing.Generated

@Generated
data class AutofillSetting(
    val isBulk: Boolean,
    val disableSetting: DisableSetting,
    val itemTypeList: List<ItemType>? = null,
) : TrackingLog {
    override val name
        get() = "autofill_setting"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_bulk"] = isBulk
        collector["disable_setting"] = disableSetting
        collector["item_type_list"] = itemTypeList
    }
}