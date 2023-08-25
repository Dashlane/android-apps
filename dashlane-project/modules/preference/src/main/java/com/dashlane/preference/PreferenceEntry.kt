package com.dashlane.preference

import android.content.SharedPreferences
import com.dashlane.util.MD5Hash

abstract class PreferenceEntry internal constructor(val key: String) {

    internal abstract fun addTo(editor: SharedPreferences.Editor)

    private class StringPreferenceEntry internal constructor(key: String, private val value: String?) :
        PreferenceEntry(key) {

        override fun addTo(editor: SharedPreferences.Editor) {
            val key = key
            editor.remove(key) 
            editor.putString(MD5Hash.hash(key), value)
        }
    }

    private class IntPreferenceEntry internal constructor(key: String, private val value: Int) : PreferenceEntry(key) {

        override fun addTo(editor: SharedPreferences.Editor) {
            val key = key
            editor.remove(key) 
            editor.putInt(MD5Hash.hash(key), value)
        }
    }

    private class FloatPreferenceEntry internal constructor(key: String, private val value: Float) :
        PreferenceEntry(key) {

        override fun addTo(editor: SharedPreferences.Editor) {
            val key = key
            editor.remove(key) 
            editor.putFloat(MD5Hash.hash(key), value)
        }
    }

    private class LongPreferenceEntry internal constructor(key: String, private val value: Long) :
        PreferenceEntry(key) {

        override fun addTo(editor: SharedPreferences.Editor) {
            val key = key
            editor.remove(key) 
            editor.putLong(MD5Hash.hash(key), value)
        }
    }

    private class BooleanPreferenceEntry internal constructor(key: String, private val value: Boolean) :
        PreferenceEntry(key) {

        override fun addTo(editor: SharedPreferences.Editor) {
            val key = key
            editor.remove(key) 
            editor.putBoolean(MD5Hash.hash(key), value)
        }
    }

    private class StringSetPreferenceEntry internal constructor(key: String, private val value: Set<String>?) :
        PreferenceEntry(key) {

        override fun addTo(editor: SharedPreferences.Editor) {
            val key = key
            editor.remove(key) 
            editor.putStringSet(MD5Hash.hash(key), value)
        }
    }

    private class RemovePreferenceEntry internal constructor(key: String) : PreferenceEntry(key) {

        override fun addTo(editor: SharedPreferences.Editor) {
            val key = key
            editor.remove(key) 
            editor.remove(MD5Hash.hash(key))
        }
    }

    companion object {

        @JvmStatic
        fun setString(label: String, value: String?): PreferenceEntry {
            return StringPreferenceEntry(label, value)
        }

        @JvmStatic
        fun setInt(label: String, value: Int): PreferenceEntry {
            return IntPreferenceEntry(label, value)
        }

        @JvmStatic
        fun setFloat(label: String, value: Float): PreferenceEntry {
            return FloatPreferenceEntry(label, value)
        }

        @JvmStatic
        fun setLong(label: String, value: Long): PreferenceEntry {
            return LongPreferenceEntry(label, value)
        }

        @JvmStatic
        fun setBoolean(label: String, value: Boolean): PreferenceEntry {
            return BooleanPreferenceEntry(label, value)
        }

        @JvmStatic
        fun setStringSet(label: String, value: Set<String>?): PreferenceEntry {
            return StringSetPreferenceEntry(label, value)
        }

        @JvmStatic
        fun toRemove(label: String): PreferenceEntry {
            return RemovePreferenceEntry(label)
        }
    }
}
