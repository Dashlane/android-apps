package com.dashlane.preference

import android.content.SharedPreferences
import com.dashlane.util.MD5Hash
import java.util.HashSet

abstract class DashlanePreferencesManager {

    protected abstract val sharedPreferences: SharedPreferences?

    fun getString(label: String): String? {
        return getString(label, null)
    }

    fun getString(label: String, defaultValue: String?): String? {
        val sharedPreferences = sharedPreferences ?: return defaultValue
        val labelObfuscated = MD5Hash.hash(label)
        CompatibilityPreferencesManager(sharedPreferences).obfuscateStringIfNeeded(label, labelObfuscated)
        return sharedPreferences.getString(labelObfuscated, defaultValue)
    }

    fun getInt(label: String): Int {
        return getInt(label, 0)
    }

    fun getInt(label: String, defaultValue: Int): Int {
        val sharedPreferences = sharedPreferences ?: return defaultValue
        val labelObfuscated = MD5Hash.hash(label)
        CompatibilityPreferencesManager(sharedPreferences).obfuscateIntIfNeeded(label, labelObfuscated)
        return sharedPreferences.getInt(labelObfuscated, defaultValue)
    }

    fun getLong(label: String): Long {
        return getLong(label, 0)
    }

    fun getLong(label: String, defaultValue: Long): Long {
        val sharedPreferences = sharedPreferences ?: return defaultValue
        val labelObfuscated = MD5Hash.hash(label)
        CompatibilityPreferencesManager(sharedPreferences).obfuscateLongIfNeeded(label, labelObfuscated)
        return sharedPreferences.getLong(labelObfuscated, defaultValue)
    }

    fun getBoolean(label: String): Boolean {
        return getBoolean(label, false)
    }

    fun getBoolean(label: String, defaultValue: Boolean): Boolean {
        val sharedPreferences = sharedPreferences ?: return defaultValue
        val labelObfuscated = MD5Hash.hash(label)
        CompatibilityPreferencesManager(sharedPreferences).obfuscateBooleanIfNeeded(label, labelObfuscated)
        return sharedPreferences.getBoolean(labelObfuscated, defaultValue)
    }

    fun getFloat(label: String): Float {
        return getFloat(label, 0F)
    }

    fun getFloat(label: String, defaultValue: Float = 0f): Float {
        val sharedPreferences = sharedPreferences ?: return defaultValue
        val labelObfuscated = MD5Hash.hash(label)
        CompatibilityPreferencesManager(sharedPreferences).obfuscateFloatIfNeeded(label, labelObfuscated)
        return sharedPreferences.getFloat(labelObfuscated, defaultValue)
    }

    fun getStringSet(label: String): Set<String>? {
        return getStringSet(label, null)
    }

    fun getStringSet(label: String, defaultValue: Set<String>?): Set<String>? {
        val sharedPreferences = sharedPreferences ?: return defaultValue
        val labelObfuscated = MD5Hash.hash(label)
        
        val stringSet = sharedPreferences.getStringSet(labelObfuscated, defaultValue)
        return stringSet?.let { HashSet(it) }
    }

    fun getList(label: String, valueSeparator: CharSequence = ";"): List<String>? {
        return getString(label)?.split(valueSeparator.toString())
    }

    fun exist(label: String): Boolean {
        return sharedPreferences?.contains(label) ?: false ||
                sharedPreferences?.contains(MD5Hash.hash(label)) ?: false
    }

    fun apply(vararg preferenceEntries: PreferenceEntry): Boolean {
        return apply(preferenceEntries.toList())
    }

    fun apply(preferenceEntries: List<PreferenceEntry>): Boolean {
        val editor = editor() ?: return false
        for (preferenceEntry in preferenceEntries) {
            preferenceEntry.addTo(editor)
        }
        editor.apply()
        return true
    }

    fun putString(label: String, value: String?): Boolean {
        return apply(PreferenceEntry.setString(label, value))
    }

    fun putInt(label: String, value: Int): Boolean {
        return apply(PreferenceEntry.setInt(label, value))
    }

    fun putFloat(label: String, value: Float): Boolean {
        return apply(PreferenceEntry.setFloat(label, value))
    }

    fun putLong(label: String, value: Long): Boolean {
        return apply(PreferenceEntry.setLong(label, value))
    }

    fun putBoolean(label: String, value: Boolean): Boolean {
        return apply(PreferenceEntry.setBoolean(label, value))
    }

    fun putStringSet(label: String, value: Set<String>?): Boolean {
        return apply(PreferenceEntry.setStringSet(label, value))
    }

    fun putList(label: String, values: List<String>?, valueSeparator: CharSequence = ";"): Boolean {
        return apply(PreferenceEntry.setString(label, values?.joinToString(valueSeparator)))
    }

    fun remove(label: String): Boolean {
        return apply(PreferenceEntry.toRemove(label))
    }

    fun clear() {
        val editor = editor()
        editor?.clear()
        editor?.apply()
    }

    @Suppress("SharedPreferencesSecurity")
    private fun editor(): SharedPreferences.Editor? {
        val sharedPreferences = sharedPreferences ?: return null
        return sharedPreferences.edit()
    }
}
