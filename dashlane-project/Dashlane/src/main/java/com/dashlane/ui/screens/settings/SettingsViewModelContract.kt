package com.dashlane.ui.screens.settings

import com.dashlane.ui.screens.settings.item.SettingScreenItem
import kotlinx.coroutines.flow.Flow

interface SettingsViewModelContract {
    val targetId: String?
    val settingScreenItem: SettingScreenItem
    val syncFeedbacks: Flow<Int>
    val use2faSettingStateChanges: Flow<Boolean>
    val accountRecoveryKeyStateChanges: Flow<Boolean>

    var shouldHighlightSetting: Boolean
    var pendingAdapterPosition: Int

    fun onRefresh()

    fun onSettingInteraction()
}