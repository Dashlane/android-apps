package com.dashlane.settings

import arrow.optics.copy
import arrow.optics.optics
import com.dashlane.util.isNotSemanticallyNull

@optics
sealed interface SettingsItemData {
    companion object

    val id: Settings
    val title: String
    val description: String
    val highlightText: String?
    val enabled: Boolean
    val visible: Boolean
    val isLoading: Boolean
    val requireLock: Boolean
    val forceMasterPassword: Boolean

    @optics
    data class Click(
        override val id: Settings.Click,
        override val title: String,
        override val description: String,
        override val highlightText: String? = null,
        override val enabled: Boolean,
        override val visible: Boolean = true,
        override val isLoading: Boolean = false,
        override val requireLock: Boolean = false,
        override val forceMasterPassword: Boolean = false,
    ) : SettingsItemData {
        companion object
    }

    @optics
    data class Toggle(
        override val id: Settings.Toggle,
        override val title: String,
        override val description: String,
        override val highlightText: String? = null,
        override val enabled: Boolean,
        override val visible: Boolean = true,
        override val isLoading: Boolean = false,
        override val requireLock: Boolean = false,
        override val forceMasterPassword: Boolean = false,
        val checked: Boolean,
    ) : SettingsItemData {
        companion object
    }
}

fun MutableList<SettingsItemData>.updateSetting(id: Settings, checked: Boolean): MutableList<SettingsItemData> {
    this.find { it.id == id }
        ?.let { setting ->
            val index = indexOf(setting)
            set(index, SettingsItemData.toggle.checked.modify(setting) { checked })
        }
    return this
}

fun MutableList<SettingsItemData>.updateSetting(
    id: Settings,
    modify: (SettingsItemData) -> SettingsItemData,
): MutableList<SettingsItemData> {
    this.find { it.id == id }
        ?.let { setting ->
            val index = indexOf(setting)
            set(index, modify(setting))
        }
    return this
}

fun MutableList<SettingsItemData>.removeSetting(id: Settings): MutableList<SettingsItemData> {
    this.find { it.id == id }?.let { setting -> remove(setting) }
    return this
}

fun List<SettingsItemData>.search(query: String): List<SettingsItemData> {
    return if (query.isNotSemanticallyNull()) {
        mapNotNull { setting ->
            if (setting.visible && (setting.title.contains(query, true) || setting.description.contains(query, true))) {
                setting.copy { SettingsItemData.highlightText set query }
            } else {
                null
            }
        }
    } else {
        emptyList()
    }
}