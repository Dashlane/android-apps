package com.dashlane.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Parcelable

interface PostAccountCreationCoordinator {
    fun startHomeScreenAfterAccountCreation(activity: Activity)
    fun newHomeIntent(
        context: Context,
        startedWithIntent: Parcelable? = null,
        fromAccountCreation: Boolean = true
    ): Intent
}