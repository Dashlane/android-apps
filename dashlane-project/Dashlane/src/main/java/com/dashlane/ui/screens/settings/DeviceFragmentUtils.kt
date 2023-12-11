package com.dashlane.ui.screens.settings

import android.content.Context
import android.text.format.DateUtils
import com.dashlane.server.api.endpoints.devices.ListDevicesService
import com.dashlane.server.api.time.toInstant
import java.time.Instant

fun ListDevicesService.Data.Device.formattedCreationDate(context: Context) = formatDateTime(context, creationDate.toInstant())

fun ListDevicesService.Data.Device.formattedUpdateDate(context: Context) = formatDateTime(context, updateDate.toInstant())

private fun formatDateTime(context: Context, instant: Instant): String? =
    DateUtils.formatDateTime(
        context,
        instant.toEpochMilli(),
        DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_YEAR or DateUtils.FORMAT_NUMERIC_DATE
    )