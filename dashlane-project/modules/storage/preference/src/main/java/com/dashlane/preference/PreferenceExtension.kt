package com.dashlane.preference

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun DashlanePreferencesManager.stringPreference(
    key: String,
    defaultValue: String? = null
): ReadWriteProperty<Any, String?> =
    PreferenceProperty(
        this,
        key,
        defaultValue,
        DashlanePreferencesManager::getString,
        DashlanePreferencesManager::putString
    )

fun DashlanePreferencesManager.booleanPreference(
    key: String,
    defaultValue: Boolean = false
): ReadWriteProperty<Any, Boolean> =
    PreferenceProperty(
        this,
        key,
        defaultValue,
        DashlanePreferencesManager::getBoolean,
        DashlanePreferencesManager::putBoolean
    )

fun DashlanePreferencesManager.intPreference(key: String, defaultValue: Int = 0): ReadWriteProperty<Any, Int> =
    PreferenceProperty(
        this,
        key,
        defaultValue,
        DashlanePreferencesManager::getInt,
        DashlanePreferencesManager::putInt
    )

fun DashlanePreferencesManager.longPreference(key: String, defaultValue: Long = 0L): ReadWriteProperty<Any, Long> =
    PreferenceProperty(
        this,
        key,
        defaultValue,
        DashlanePreferencesManager::getLong,
        DashlanePreferencesManager::putLong
    )

private class PreferenceProperty<T>(
    val preferencesManager: DashlanePreferencesManager,
    val key: String,
    val defaultValue: T,
    val readValue: DashlanePreferencesManager.(String, T) -> T,
    val writeValue: DashlanePreferencesManager.(String, T) -> Any
) : ReadWriteProperty<Any, T> {

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        return preferencesManager.readValue(key, defaultValue)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        preferencesManager.writeValue(key, value)
    }
}