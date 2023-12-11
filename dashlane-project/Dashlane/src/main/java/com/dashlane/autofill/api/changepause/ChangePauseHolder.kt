package com.dashlane.autofill.api.changepause

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import com.dashlane.R
import com.dashlane.autofill.changepause.view.ChangePauseViewTypeProviderFactory
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class ChangePauseHolder(v: View) : EfficientViewHolder<ChangePauseViewTypeProviderFactory.PauseSetting>(v) {
    private val settingsTitle: TextView = v.findViewById(R.id.setting_title)
    private val settingsDescription: TextView = v.findViewById(R.id.setting_description)
    private val settingTrailing: View = v.findViewById(R.id.setting_trailing)
    private val authentifiantIcon: SwitchCompat = v.findViewById(R.id.setting_checkbox)
    private val toggleTitle = v.context.getString(R.string.autofill_changepause_toggle_title)

    override fun updateView(context: Context, itemWrapper: ChangePauseViewTypeProviderFactory.PauseSetting?) {
        settingTrailing.isVisible = true
        authentifiantIcon.isVisible = true
        itemWrapper?.let {
            updateView(toggleTitle, it.pauseUntilString, it.isPaused)
        } ?: updateToEmptyState()
    }

    private fun updateView(title: String, subtitle: String, isToggle: Boolean) {
        settingsTitle.text = title
        settingsDescription.text = subtitle
        authentifiantIcon.isChecked = isToggle
        authentifiantIcon.isEnabled = true
    }

    private fun updateToEmptyState() {
        settingsTitle.text = null
        settingsDescription.text = null
        authentifiantIcon.isChecked = false
        authentifiantIcon.isEnabled = false
    }
}
