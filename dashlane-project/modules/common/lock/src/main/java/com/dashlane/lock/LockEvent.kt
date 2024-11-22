package com.dashlane.lock

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class LockEvent {
    data object Lock : LockEvent()

    data object Cancelled : LockEvent()

    data class Unlock(
        val reason: Reason? = null,
        val lockType: LockType,
    ) : LockEvent() {

        sealed class Reason : Parcelable {
            @Parcelize
            data object Unknown : Reason()

            @Parcelize
            data object AppAccess : Reason()

            @Parcelize
            data class WithCode(val requestCode: Int, val origin: Origin? = null) : Reason() {
                enum class Origin { EDIT_SETTINGS, CHANGE_MASTER_PASSWORD }
            }

            @Parcelize
            data class OpenItem(val xmlObjectName: String, val itemUid: String) : Reason()

            @SuppressLint("ParcelCreator")
            @Parcelize
            data class AccessFromExternalComponent(val itemUid: String?) : Reason()

            @SuppressLint("ParcelCreator")
            @Parcelize
            data object AccessFromAutofillApi : Reason()

            @SuppressLint("ParcelCreator")
            @Parcelize
            data object AccessFromFollowUpNotification : Reason()
        }
    }
}