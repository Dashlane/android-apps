package com.dashlane.session

import com.dashlane.user.Username
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
fun SessionRestorer.startRestoreSession(username: Username?) = GlobalScope.launch {
    restore(username)
}