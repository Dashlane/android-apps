package com.dashlane.login.monobucket

sealed class MonobucketState {
    object Idle : MonobucketState()
    object CanceledUnregisterDevice : MonobucketState()
    object ConfirmUnregisterDevice : MonobucketState()
    object UserLoggedOut : MonobucketState()
}