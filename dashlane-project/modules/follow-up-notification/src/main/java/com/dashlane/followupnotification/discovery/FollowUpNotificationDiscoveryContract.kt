package com.dashlane.followupnotification.discovery

import androidx.annotation.StringRes
import com.skocken.presentation.definition.Base

interface FollowUpNotificationDiscoveryContract {
    interface ViewProxy : Base.IView {
        fun setScreenContent(content: ScreenContent)
        fun setOnButtonClick(onClick: () -> Unit)
    }

    interface Presenter : Base.IPresenter {
        fun setupDiscoveryScreen(isReminder: Boolean)
    }

    interface DataProvider : Base.IDataProvider {
        fun getScreenContent(config: ScreenConfiguration): ScreenContent
    }
}

data class ScreenContent(val hasTopIcon: Boolean, @StringRes val title: Int, @StringRes val description: Int)

enum class ScreenConfiguration {
    INTRODUCTION,
    REMINDER
}