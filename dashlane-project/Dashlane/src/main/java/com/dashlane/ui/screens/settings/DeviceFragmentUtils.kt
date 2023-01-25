package com.dashlane.ui.screens.settings

import android.text.format.DateUtils
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.server.api.endpoints.devices.ListDevicesService
import com.dashlane.server.api.time.toInstant
import java.time.Instant

val ListDevicesService.Data.Device.formattedCreationDate
    get() = formatDateTime(creationDate.toInstant())

val ListDevicesService.Data.Device.formattedUpdateDate
    get() = formatDateTime(updateDate.toInstant())

private fun formatDateTime(instant: Instant): String? =
    DateUtils.formatDateTime(
        SingletonProvider.getContext(),
        instant.toEpochMilli(),
        DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_NUMERIC_DATE
    )