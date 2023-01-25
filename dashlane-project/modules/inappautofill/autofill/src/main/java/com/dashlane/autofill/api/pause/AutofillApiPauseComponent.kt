package com.dashlane.autofill.api.pause

import android.content.Context
import com.dashlane.autofill.api.pause.services.PausedFormSourcesProvider
import com.dashlane.autofill.api.pause.services.PausedFormSourcesRepository
import com.dashlane.autofill.api.pause.services.PausedFormSourcesStringsRepository
import com.dashlane.autofill.api.pause.services.RemovePauseContract



interface AutofillApiPauseComponent {
    val pausedFormSourcesRepository: PausedFormSourcesRepository
    val pausedFormSourcesProvider: PausedFormSourcesProvider
    val pausedFormSourcesStringsRepository: PausedFormSourcesStringsRepository
    val autofillApiPauseLogger: AutofillApiPauseLogger
    val removePauseContract: RemovePauseContract

    companion object {
        operator fun invoke(context: Context) =
            (context.applicationContext as AutofillApiPauseApplication).component
    }
}
