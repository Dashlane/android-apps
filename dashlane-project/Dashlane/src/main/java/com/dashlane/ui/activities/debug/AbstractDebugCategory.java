package com.dashlane.ui.activities.debug;


import android.app.Activity;

import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;



abstract class AbstractDebugCategory {

    private final Activity mDebugActivity;

    AbstractDebugCategory(Activity debugActivity) {
        mDebugActivity = debugActivity;
    }

    final void add(PreferenceScreen group) {
        PreferenceCategory category = addCategory(group, getName());
        addSubItems(category);
    }

    abstract void addSubItems(PreferenceGroup group);

    abstract String getName();

    Activity getDebugActivity() {
        return mDebugActivity;
    }

    PreferenceCategory addCategory(PreferenceGroup group, String title) {
        PreferenceCategory category = new PreferenceCategory(mDebugActivity);
        category.setTitle(title);
        group.addPreference(category);
        return category;
    }

    void addPreferenceButton(PreferenceGroup group, String title,
                             Preference.OnPreferenceClickListener clickListener) {
        addPreferenceButton(group, title, null, clickListener);
    }

    void addPreferenceButton(PreferenceGroup group, String title, String description,
                             Preference.OnPreferenceClickListener clickListener) {
        Preference preference = new Preference(mDebugActivity);
        preference.setTitle(title);
        preference.setSummary(description);
        preference.setOnPreferenceClickListener(clickListener);
        group.addPreference(preference);
    }

    void addPreferenceCheckbox(PreferenceGroup group, String title, String description, boolean isChecked,
                               boolean isSelectable, Preference.OnPreferenceClickListener clickListener) {
        CheckBoxPreference preference = new CheckBoxPreference(mDebugActivity);
        preference.setTitle(title);
        preference.setSummary(description);
        preference.setChecked(isChecked);
        preference.setSelectable(isSelectable);
        preference.setOnPreferenceClickListener(clickListener);
        group.addPreference(preference);
    }
}
