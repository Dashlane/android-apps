package com.dashlane.csvexport

import android.net.Uri
import com.dashlane.mvvm.State

data class CSVExportViewState(
    val isLoading: Boolean = false,
) : State.View

sealed class CSVExportNavigationEffect : State.SideEffect {
    data object Success : CSVExportNavigationEffect()

    data object Failure : CSVExportNavigationEffect()

    data class FailureShare(val files: List<Uri>) : CSVExportNavigationEffect()

    data class OpenLink(val link: String) : CSVExportNavigationEffect()
}