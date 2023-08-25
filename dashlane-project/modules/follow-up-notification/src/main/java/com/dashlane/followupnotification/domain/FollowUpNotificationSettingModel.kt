package com.dashlane.followupnotification.domain

data class FollowUpNotificationSettingModel(
    val isChecked: Boolean,
    val title: String,
    val description: String
)