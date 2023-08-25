package com.dashlane.preference

import android.content.SharedPreferences

@Suppress("SharedPreferencesSecurity")
class CompatibilityPreferencesManager internal constructor(private val sharedPreferences: SharedPreferences) {

    internal fun obfuscateStringIfNeeded(label: String, obfuscatedLabel: String) {
        if (sharedPreferences.contains(label)) {
            val data = sharedPreferences.getString(label, "")
            val editor = sharedPreferences.edit()
            editor.remove(label)
            editor.putString(obfuscatedLabel, data)
            editor.apply()
        }
    }

    internal fun obfuscateBooleanIfNeeded(label: String, obfuscatedLabel: String) {
        if (sharedPreferences.contains(label)) {
            val data = sharedPreferences.getBoolean(label, false)
            val editor = sharedPreferences.edit()
            editor.remove(label)
            editor.putBoolean(obfuscatedLabel, data)
            editor.apply()
        }
    }

    internal fun obfuscateIntIfNeeded(label: String, obfuscatedLabel: String) {
        if (sharedPreferences.contains(label)) {
            val data = sharedPreferences.getInt(label, 0)
            val editor = sharedPreferences.edit()
            editor.remove(label)
            editor.putInt(obfuscatedLabel, data)
            editor.apply()
        }
    }

    internal fun obfuscateFloatIfNeeded(label: String, obfuscatedLabel: String) {
        if (sharedPreferences.contains(label)) {
            val data = sharedPreferences.getFloat(label, 0f)
            val editor = sharedPreferences.edit()
            editor.remove(label)
            editor.putFloat(obfuscatedLabel, data)
            editor.apply()
        }
    }

    internal fun obfuscateLongIfNeeded(label: String, obfuscatedLabel: String) {
        if (sharedPreferences.contains(label)) {
            val data = sharedPreferences.getLong(label, 0)
            val editor = sharedPreferences.edit()
            editor.remove(label)
            editor.putLong(obfuscatedLabel, data)
            editor.apply()
        }
    }
}
