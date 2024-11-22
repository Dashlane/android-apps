package com.dashlane.session.repository

import android.content.Context
import android.content.SharedPreferences
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.util.MD5Hash

class UserLoggedIn(
    private val context: Context,
    manager: SessionManager
) : UserPreferencesManager() {

    override var sharedPreferences: SharedPreferences? =
        manager.session?.username?.email?.let { context.getSharedPreferences(MD5Hash.hash(it), Context.MODE_PRIVATE) }
}