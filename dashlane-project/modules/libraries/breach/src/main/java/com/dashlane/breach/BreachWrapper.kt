package com.dashlane.breach

import android.annotation.SuppressLint
import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@SuppressLint("ParcelCreator")
@Keep
@Parcelize
data class BreachWrapper(
    val localBreach: AnalyzedBreach,
    val publicBreach: Breach,
    val linkedAuthentifiant: List<String>,
    val isForPopup: Boolean = false,
) : Parcelable