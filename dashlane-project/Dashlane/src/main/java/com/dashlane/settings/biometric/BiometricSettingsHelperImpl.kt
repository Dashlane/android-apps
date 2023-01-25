package com.dashlane.settings.biometric

import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.widget.SwitchCompat
import com.dashlane.R
import com.dashlane.util.CheckedDelegate
import com.dashlane.util.showToaster

class BiometricSettingsHelperImpl(val rootView: View, val logger: BiometricSettingsLogger) : BiometricSettingsHelper {
    private val biometricSetting = rootView.findViewById<View>(R.id.setting_biometric)!!
    private val biometricToggle = biometricSetting.findViewById<SwitchCompat>(R.id.setting_biometric_checkbox)!!
    override var biometricSettingChecked: Boolean by CheckedDelegate(biometricToggle)

    private val resetMpSetting = rootView.findViewById<View>(R.id.setting_resetmp)!!
    private val resetMpToggle = resetMpSetting.findViewById<SwitchCompat>(R.id.setting_resetmp_checkbox)!!
    override var resetMpSettingChecked: Boolean by CheckedDelegate(resetMpToggle)

    private val settingsLayout = rootView.findViewById<View>(R.id.settings_layout)!!
    private val infoLayout = rootView.findViewById<View>(R.id.info_layout)!!

    init {
        initBiometric()
        initResetMp()
        rootView.findViewById<View>(R.id.settings_info_back_btn)?.setOnClickListener { hideInfo() }
        rootView.findViewById<View>(R.id.settings_info_show_btn)?.setOnClickListener { showInfo() }
    }

    private fun showInfo() {
        logger.logShowFAQ()
        settingsLayout.visibility = View.GONE
        infoLayout.visibility = View.VISIBLE
    }

    private fun hideInfo() {
        settingsLayout.visibility = View.VISIBLE
        infoLayout.visibility = View.GONE
    }

    private fun initBiometric() {
        rootView.findViewById<TextView>(R.id.setting_biometric_title)
            ?.setText(R.string.create_account_settings_biometric)
        rootView.findViewById<TextView>(R.id.setting_biometric_description)
            ?.setText(R.string.create_account_settings_biometric_desc)
        biometricToggle.isChecked = true
        biometricSetting.setOnClickListener {
            biometricToggle.toggle()
            onBiometricSettingChanged(biometricSettingChecked)
        }
    }

    private fun initResetMp() {
        rootView.findViewById<TextView>(R.id.setting_resetmp_title)?.setText(R.string.create_account_settings_resetmp)
        rootView.findViewById<TextView>(R.id.setting_resetmp_description)
            ?.setText(R.string.create_account_settings_resetmp_desc)
        resetMpToggle.isChecked = true
        resetMpSetting.setOnClickListener {
            resetMpToggle.toggle()
            onResetMpSettingChanged(resetMpSettingChecked)
        }
    }

    private fun onBiometricSettingChanged(enabled: Boolean) {
        if (!enabled && resetMpSettingChecked) {
            resetMpSettingChecked = false
            showWarning(R.string.create_account_settings_warning_toast)
        }
    }

    private fun onResetMpSettingChanged(enabled: Boolean) {
        if (enabled && !biometricSettingChecked) {
            biometricSettingChecked = true
            showWarning(R.string.create_account_settings_warning_toast)
        }
    }

    private fun showWarning(@StringRes warningResId: Int) {
        rootView.context.showToaster(warningResId, Toast.LENGTH_SHORT)
    }
}