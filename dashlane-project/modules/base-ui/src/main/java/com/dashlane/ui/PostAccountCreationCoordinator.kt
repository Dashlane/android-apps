package com.dashlane.ui

import android.content.Intent

interface PostAccountCreationCoordinator {
    fun getHomeScreenAfterAccountCreationIntent(): Intent
    fun newHomeIntent(): Intent
}