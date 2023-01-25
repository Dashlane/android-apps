package com.dashlane.security.identitydashboard.breach

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.annotation.Keep
import com.dashlane.breach.Breach
import kotlinx.parcelize.Parcelize



@SuppressLint("ParcelCreator")
@Keep
@Parcelize
data class BreachWrapper(
    val localBreach: AnalyzedBreach,
    val publicBreach: Breach,
    val linkedAuthentifiant: List<String>,
    val isForPopup: Boolean = false
) : Parcelable