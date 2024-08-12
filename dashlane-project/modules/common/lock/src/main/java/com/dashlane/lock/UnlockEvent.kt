package com.dashlane.lock

import android.annotation.SuppressLint
import android.os.Parcelable
import com.dashlane.event.AppEvent
import kotlinx.parcelize.Parcelize

@SuppressLint("ParcelCreator")
@Parcelize
data class UnlockEvent(private val success: Boolean, val reason: Reason) : Parcelable, AppEvent {

    fun isSuccess() = success

    interface Reason : Parcelable {

        @SuppressLint("ParcelCreator")
        @Parcelize
        class Unknown : Reason

        @SuppressLint("ParcelCreator")
        @Parcelize
        class AppAccess : Reason

        @SuppressLint("ParcelCreator")
        @Parcelize
        data class WithCode(val requestCode: Int, val origin: Origin? = null) : Reason {
            enum class Origin {
                EDIT_SETTINGS,
                CHANGE_MASTER_PASSWORD
            }
        }

        @SuppressLint("ParcelCreator")
        @Parcelize
        data class OpenItem(val xmlObjectName: String, val itemUid: String) : Reason

        @SuppressLint("ParcelCreator")
        @Parcelize
        data class AccessFromExternalComponent(val itemUid: String?) : Reason

        @SuppressLint("ParcelCreator")
        @Parcelize
        class AccessFromAutofillApi : Reason

        @SuppressLint("ParcelCreator")
        @Parcelize
        class AccessFromFollowUpNotification : Reason

        @SuppressLint("ParcelCreator")
        @Parcelize
        class PairAuthenticatorApp : Reason
    }
}