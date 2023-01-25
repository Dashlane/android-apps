package com.dashlane.autofill.api.pause

import com.dashlane.autofill.api.pause.model.PauseDurations
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import kotlinx.coroutines.CoroutineScope



interface AskPauseContract {
    

    interface View {
        fun showPauseTitle(title: String)
        fun showPauseMessage(message: String, pauseDurations: PauseDurations)
        fun showPauseErrorMessage()
    }

    

    interface Presenter {
        fun setView(view: View, viewCoroutineScope: CoroutineScope)
        fun onResume(autoFillFormSource: AutoFillFormSource)
        fun onPermanentPauseButtonClick(autoFillFormSource: AutoFillFormSource)
        fun onOneHourPauseButtonClick(autoFillFormSource: AutoFillFormSource)
        fun onOneDayPauseButtonClick(autoFillFormSource: AutoFillFormSource)
    }

    

    interface DataProvider {
        suspend fun getPauseFormSourceTitle(autoFillFormSource: AutoFillFormSource): String
        suspend fun pauseItem(autoFillFormSource: AutoFillFormSource, pauseDurations: PauseDurations)
        suspend fun getPauseMessageForDuration(
            autoFillFormSource: AutoFillFormSource,
            pauseDurations: PauseDurations
        ): String
    }
}
