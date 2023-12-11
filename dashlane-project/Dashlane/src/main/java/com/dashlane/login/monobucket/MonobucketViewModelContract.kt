package com.dashlane.login.monobucket

import com.dashlane.login.Device
import kotlinx.coroutines.flow.StateFlow

interface MonobucketViewModelContract {
    val monobucketOwner: Device?
    val state: StateFlow<MonobucketState>

    fun onUpgradePremium()
    fun onUnlinkPreviousDevice()
    fun onConfirmUnregisterDevice()
    fun onCancelUnregisterDevice()
    fun onLogOut()
    fun hasSync(): Boolean
}