package com.dashlane.vault.textfactory.list

import android.content.res.Resources
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.R
import javax.inject.Inject

class WiFiListTextFactory @Inject constructor(
    private val resources: Resources,
) : DataIdentifierListTextFactory<SummaryObject.WiFi> {
    override fun getTitle(item: SummaryObject.WiFi): StatusText {
        val name = item.name.takeIf { it.isNotSemanticallyNull() }
        val ssid = item.ssid.takeIf { it.isNotSemanticallyNull() }
        return StatusText(name ?: ssid ?: resources.getString(R.string.wifi))
    }

    override fun getDescription(item: SummaryObject.WiFi, default: StatusText): StatusText {
        val ssid = item.ssid.takeIf { it.isNotSemanticallyNull() }

        return if (ssid != null) {
            StatusText(ssid)
        } else {
            default
        }
    }
}