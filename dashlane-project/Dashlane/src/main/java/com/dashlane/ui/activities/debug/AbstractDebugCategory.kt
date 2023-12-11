package com.dashlane.ui.activities.debug

import android.content.Context
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceScreen

internal abstract class AbstractDebugCategory {
    abstract val context: Context
    abstract val name: String?

    fun add(group: PreferenceScreen) {
        val category = addCategory(group, name)
        addSubItems(category)
    }

    abstract fun addSubItems(group: PreferenceGroup)

    private fun addCategory(group: PreferenceGroup, title: String?): PreferenceCategory {
        val category = PreferenceCategory(context)
        category.title = title
        group.addPreference(category)
        return category
    }

    fun addPreferenceButton(
        group: PreferenceGroup,
        title: String?,
        clickListener: Preference.OnPreferenceClickListener?
    ) {
        addPreferenceButton(group, title, null, clickListener)
    }

    fun addPreferenceButton(
        group: PreferenceGroup,
        title: String?,
        description: String?,
        clickListener: Preference.OnPreferenceClickListener?
    ) {
        val preference = Preference(context)
        preference.title = title
        preference.summary = description
        preference.onPreferenceClickListener = clickListener
        group.addPreference(preference)
    }

    fun addPreferenceCheckbox(
        group: PreferenceGroup,
        title: String?,
        description: String?,
        isChecked: Boolean,
        isSelectable: Boolean,
        clickListener: Preference.OnPreferenceClickListener?
    ) {
        val preference = CheckBoxPreference(context)
        preference.title = title
        preference.summary = description
        preference.isChecked = isChecked
        preference.isSelectable = isSelectable
        preference.onPreferenceClickListener = clickListener
        group.addPreference(preference)
    }
}