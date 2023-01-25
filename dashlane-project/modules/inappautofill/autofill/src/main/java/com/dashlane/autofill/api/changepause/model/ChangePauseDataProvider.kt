package com.dashlane.autofill.api.changepause.model

import com.dashlane.autofill.api.changepause.AutofillApiChangePauseLogger
import com.dashlane.autofill.api.changepause.ChangePauseContract
import com.dashlane.autofill.api.changepause.dagger.Data
import com.dashlane.autofill.api.changepause.dagger.ViewModel
import com.dashlane.autofill.api.changepause.services.ChangePauseStrings
import com.dashlane.autofill.api.pause.model.PausedFormSource
import com.dashlane.autofill.api.pause.services.RemovePauseContract
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext



class ChangePauseDataProvider @Inject constructor(
    private val autoFillFormSource: AutoFillFormSource,
    private val removePauseContract: RemovePauseContract,
    private val changePauseStrings: ChangePauseStrings,
    private val autofillApiChangePauseLogger: AutofillApiChangePauseLogger,
    @ViewModel
    private val viewModelScope: CoroutineScope,
    @Data
    private val coroutineContext: CoroutineContext = Dispatchers.IO
) : ChangePauseContract.DataProvider {
    private val mutex = Mutex()
    private var lastState: ChangePauseModel? = null
    private var responses: ChangePauseContract.DataProvider.Responses? = null

    override fun bindResponses(responses: ChangePauseContract.DataProvider.Responses) {
        this.responses = responses
    }

    override fun currentState(): ChangePauseModel? {
        return lastState
    }

    override fun loadPause() {
        viewModelScope.launch(coroutineContext) {
            mutex.withLock {
                var loadFormSourcesState = lastState?.copy(processing = true)
                    ?: buildNoPaused(true)
                try {
                    lastState = loadFormSourcesState
                    loadFormSourcesState = getPausedFormSource(loadFormSourcesState.autoFillFormSource)
                        .toChangeModel(processing = false)
                    lastState = loadFormSourcesState
                    responses?.updatePause(loadFormSourcesState)
                } catch (e: Exception) {
                    loadFormSourcesState = loadFormSourcesState.copy(processing = false)
                    lastState = loadFormSourcesState
                    responses?.errorOnLoadPause()
                }
            }
        }
    }

    override fun togglePause() {
        if (!mutex.isLocked) {
            viewModelScope.launch(coroutineContext) {
                mutex.withLock {
                    try {
                        var workingState = lastState?.copy(processing = true)
                            ?: throw IllegalStateException("toggle over invalid state")

                        val currentState = getPausedFormSource(workingState.autoFillFormSource).toChangeModel()

                        val syncState = workingState.pauseUntil == currentState.pauseUntil

                        if (syncState) {
                            if (workingState.isPaused) {
                                removePauseContract.removePause(workingState.autoFillFormSource)
                                workingState = buildNoPaused(false)
                                lastState = workingState
                                autofillApiChangePauseLogger.resumeFormSource(workingState.autoFillFormSource)
                                responses?.resumedPause(workingState)
                            } else {
                                autofillApiChangePauseLogger.openPauseForFormSource(workingState.autoFillFormSource)
                                responses?.openPauseDialog(workingState.autoFillFormSource)
                            }
                        } else {
                            lastState = currentState
                            responses?.updatePause(currentState)
                        }
                    } catch (e: Exception) {
                        responses?.errorOnTogglePause()
                    }
                }
            }
        } else {
            responses?.errorOnTogglePause()
        }
    }

    private suspend fun getPausedFormSource(autoFillFormSource: AutoFillFormSource): PausedFormSource? {
        return removePauseContract.getPausedFormSource(autoFillFormSource)
    }

    private fun PausedFormSource?.toChangeModel(processing: Boolean = false): ChangePauseModel {
        val pausedFormSource = this ?: return buildNoPaused(processing)
        val formSource = pausedFormSource.autoFillFormSource
        val formSourceTitle = changePauseStrings.getAutofillFromSourceTitle(formSource)
        val pauseTitle = changePauseStrings.getPauseTitle(formSource)
        val pauseSubtitle = getPauseDurationMessage(formSource, pausedFormSource.pauseUntil)

        return ChangePauseModel(processing, formSource, pauseUntil, formSourceTitle, pauseTitle, pauseSubtitle)
    }

    private fun buildNoPaused(processing: Boolean = false): ChangePauseModel {
        val formSourceTitle = changePauseStrings.getAutofillFromSourceTitle(this.autoFillFormSource)
        val pauseTitle = changePauseStrings.getPauseTitle(this.autoFillFormSource)
        val pauseSubtitle = changePauseStrings.getNotPausedMessage(this.autoFillFormSource)

        return ChangePauseModel(
            processing,
            this.autoFillFormSource,
            null,
            formSourceTitle,
            pauseTitle,
            pauseSubtitle
        )
    }

    private fun getPauseDurationMessage(autoFillFormSource: AutoFillFormSource, pauseUntil: Instant): String {
        if (pauseUntil == Instant.MAX) {
            return changePauseStrings.getPausePermanentMessage(autoFillFormSource)
        }

        val duration: Duration = Duration.between(Instant.now(), pauseUntil)
        val hour = Duration.ofHours(1L)

        return if (duration <= hour) {
            changePauseStrings.getPauseForMinutesMessage(autoFillFormSource, duration.toMinutes().toInt())
        } else {
            changePauseStrings.getPauseForHoursMessage(autoFillFormSource, duration.toHours().toInt())
        }
    }
}
