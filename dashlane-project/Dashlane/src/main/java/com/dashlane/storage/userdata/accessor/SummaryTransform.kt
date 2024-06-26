package com.dashlane.storage.userdata.accessor

import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

interface SummaryTransform {
    operator fun invoke(summary: SummaryObject): SummaryObject

    class Provider @Inject constructor(
        private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>
    ) {
        fun get() = if (teamSpaceAccessorProvider.get()?.isSsoUser == true) {
            
            ClearSecureNoteLocks
        } else {
            None
        }
    }

    private object None : SummaryTransform {
        override fun invoke(summary: SummaryObject) = summary
    }

    private object ClearSecureNoteLocks : SummaryTransform {
        override fun invoke(summary: SummaryObject) = when {
            summary is SummaryObject.SecureNote && summary.secured == true -> summary.copy(secured = false)
            else -> summary
        }
    }
}