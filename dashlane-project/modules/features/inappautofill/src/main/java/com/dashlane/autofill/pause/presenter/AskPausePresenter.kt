package com.dashlane.autofill.pause.presenter

import com.dashlane.autofill.pause.model.PauseDurations
import com.dashlane.autofill.pause.AskPauseContract
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.util.inject.qualifiers.FragmentLifecycleCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class AskPausePresenter @Inject constructor(
    private val dataProvider: AskPauseContract.DataProvider,
    @FragmentLifecycleCoroutineScope
    private val viewCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainDispatcher: CoroutineDispatcher
) : AskPauseContract.Presenter {

    private var view: AskPauseContract.View? = null

    override fun setView(view: AskPauseContract.View) {
        this.view = view
    }

    override fun onResume(autoFillFormSource: AutoFillFormSource, openInDashlane: Boolean) {
        getPauseFromSourceTitle(autoFillFormSource, openInDashlane)
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

    private fun getPauseFromSourceTitle(pausedFormSource: AutoFillFormSource, openInDashlane: Boolean) {
        val view = view ?: return

        viewCoroutineScope.launch(mainDispatcher) {
            try {
                val formSourceLabel = dataProvider.getPauseFormSourceTitle(pausedFormSource, openInDashlane)
                view.showPauseTitle(formSourceLabel)
            } catch (e: Exception) {
                view.showPauseErrorMessage()
            }
        }
    }

    private fun pauseFormSource(pausedFormSource: AutoFillFormSource, pauseDurations: PauseDurations) {
        val view = view ?: return

        viewCoroutineScope.launch(mainDispatcher) {
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
