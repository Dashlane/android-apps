package com.dashlane.ui.activities.debug

import android.content.Context
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceManager
import com.dashlane.BuildConfig
import com.dashlane.R
import com.dashlane.device.DeviceInfoRepository
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.ToasterImpl
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

internal class DebugCategoryDevOption @Inject constructor(
    @ActivityContext override val context: Context,
    val deviceInfoRepository: DeviceInfoRepository
) : AbstractDebugCategory() {

    override val name: String
        get() = "Dev options"

    override fun addSubItems(group: PreferenceGroup) {
        if (!BuildConfig.DEBUG) {
            throw IllegalAccessError("You cannot be there in production")
        }
        addReadSharedPreference(group)
    }

    private fun addReadSharedPreference(group: PreferenceGroup) {
        val anonymousDeviceId = deviceInfoRepository.anonymousDeviceId
        
        
        
        val prefKey = "install_receiver_adjust_sent_for_$anonymousDeviceId"
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val referer = prefs.getString(prefKey, null)
        ToasterImpl(context).show("Referer: $referer", Toast.LENGTH_LONG)
        addPreferenceButton(
            group,
            "Read all SharedPreferences"
        ) {
            val sb = StringBuilder()
            prefs.all.forEach { (key, value) ->
                sb.append("-- '").append(key).append("': ").append(value).append("\n")
            }
            DialogHelper().builder(ContextThemeWrapper(context, R.style.Theme_Dashlane))
                .setTitle("Debug SharedPreferences")
                .setMessage(sb.toString())
                .show()
            true
        }
    }
}