package com.dashlane.abtesting

import com.dashlane.abtesting.LocalVariant.ADJUST_TRACKING_ANONYMOUS_DEVICE_ID
import com.dashlane.abtesting.LocalVariant.ADJUST_TRACKING_INSTALLATION_ID



enum class LocalAbTest(val testName: String, val variants: Map<Variant, Int>) {
    ADJUST_ID_MIGRATION(
        "adjustidmigration",
        mapOf(
            ADJUST_TRACKING_ANONYMOUS_DEVICE_ID to 50,
            ADJUST_TRACKING_INSTALLATION_ID to 50
        )
    )
}

@Suppress("Unused", "EmptyClassBlock")
object LocalVariant {
    


    @JvmField
    val ADJUST_TRACKING_ANONYMOUS_DEVICE_ID = Variant("anonymousdeviceid")
    @JvmField
    val ADJUST_TRACKING_INSTALLATION_ID = Variant("installationid")
}
