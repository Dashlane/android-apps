package com.dashlane.autofill.api.pause.presenter

import com.dashlane.autofill.api.pause.model.PauseDurations
import com.dashlane.autofill.api.pause.AskPauseContract
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class AskPausePresenter(
    private val dataProvider: AskPauseContract.DataProvider
) : AskPauseContract.Presenter {

    private var view: AskPauseContract.View? = null
    private var viewCoroutineScope: CoroutineScope? = null

    override fun setView(view: AskPauseContract.View, viewCoroutineScope: CoroutineScope) {
        this.view = view
        this.viewCoroutineScope = viewCoroutineScope
    }

    override fun onResume(autoFillFormSource: AutoFillFormSource) {
        getPauseFromSourceTitle(autoFillFormSource)
    }

    override fun onPermanentPauseButtonClick(autoFillFormSource: AutoFillFormSource) {
        pauseFormSource(autoFillFormSource, PauseDurations.PERMANENT)
    }

    override fun onOneHourPauseButtonClick(autoFillFormSource: AutoFillFormSource) {
        pauseFormSource(autoFillFormSource, PauseDurations.ONE_HOUR)
    }

    override fun onOneDayPauseButtonClick(autoFillFormSource: AutoFillFormSource) {
        pauseFormSource(autoFillFormSource, PauseDurations.ONE_DAY)
    }

    private fun getPauseFromSourceTitle(pausedFormSource: AutoFillFormSource) {
        val view = view ?: return
        val viewCoroutineScope = viewCoroutineScope ?: return

        viewCoroutineScope.launch(Dispatchers.Main) {
            try {
                val formSourceLabel = dataProvider.getPauseFormSourceTitle(pausedFormSource)
                view.showPauseTitle(formSourceLabel)
            } catch (e: Exception) {
                view.showPauseErrorMessage()
            }
        }
    }

    private fun pauseFormSource(pausedFormSource: AutoFillFormSource, pauseDurations: PauseDurations) {
        val view = view ?: return
        val viewCoroutineScope = viewCoroutineScope ?: return

        viewCoroutineScope.launch(Dispatchers.Main) {
            try {
                val message = dataProvider.getPauseMessageForDuration(pausedFormSource, pauseDurations)
                dataProvider.pauseItem(pausedFormSource, pauseDurations)
                view.showPauseMessage(message, pauseDurations)
            } catch (e: Exception) {
                view.showPauseErrorMessage()
            }
        }
    }
}
