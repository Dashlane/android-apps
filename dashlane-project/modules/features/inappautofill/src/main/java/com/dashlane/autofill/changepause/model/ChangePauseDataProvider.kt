package com.dashlane.autofill.changepause.model

import com.dashlane.autofill.changepause.ChangePauseContract
import com.dashlane.autofill.changepause.services.ChangePauseStrings
import com.dashlane.autofill.pause.model.PausedFormSource
import com.dashlane.autofill.pause.services.RemovePauseContract
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.util.inject.qualifiers.FragmentLifecycleCoroutineScope
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class ChangePauseDataProvider @Inject constructor(
    private val removePauseContract: RemovePauseContract,
    private val changePauseStrings: ChangePauseStrings,
    @FragmentLifecycleCoroutineScope
    private val viewModelScope: CoroutineScope,
    @IoCoroutineDispatcher
    private val ioDispatcher: CoroutineDispatcher
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

    override fun loadPause(autoFillFormSource: AutoFillFormSource) {
        viewModelScope.launch(ioDispatcher) {
            mutex.withLock {
                var loadFormSourcesState = lastState?.copy(processing = true)
                    ?: buildNoPaused(true, autoFillFormSource)
                try {
                    lastState = loadFormSourcesState
                    loadFormSourcesState = getPausedFormSource(loadFormSourcesState.autoFillFormSource)
                        .toChangeModel(processing = false, autoFillFormSource)
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

    override fun togglePause(autoFillFormSource: AutoFillFormSource) {
        if (!mutex.isLocked) {
            viewModelScope.launch(ioDispatcher) {
                mutex.withLock {
                    try {
                        var workingState = lastState?.copy(processing = true)
                            ?: throw IllegalStateException("toggle over invalid state")

                        val currentState = getPausedFormSource(workingState.autoFillFormSource)
                            .toChangeModel(autoFillFormSource = autoFillFormSource)

                        val syncState = workingState.pauseUntil == currentState.pauseUntil

                        if (syncState) {
                            if (workingState.isPaused) {
                                removePauseContract.removePause(workingState.autoFillFormSource)
                                workingState = buildNoPaused(false, autoFillFormSource)
                                lastState = workingState
                                responses?.resumedPause(workingState)
                            } else {
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

    private fun PausedFormSource?.toChangeModel(processing: Boolean = false, autoFillFormSource: AutoFillFormSource): ChangePauseModel {
        val pausedFormSource = this ?: return buildNoPaused(processing, autoFillFormSource)
        val formSource = pausedFormSource.autoFillFormSource
        val formSourceTitle = changePauseStrings.getAutofillFromSourceTitle(formSource)
        val pauseTitle = changePauseStrings.getPauseTitle(formSource)
        val pauseSubtitle = getPauseDurationMessage(formSource, pausedFormSource.pauseUntil)

        return ChangePauseModel(processing, formSource, pauseUntil, formSourceTitle, pauseTitle, pauseSubtitle)
    }

    private fun buildNoPaused(processing: Boolean = false, autoFillFormSource: AutoFillFormSource): ChangePauseModel {
        val formSourceTitle = changePauseStrings.getAutofillFromSourceTitle(autoFillFormSource)
        val pauseTitle = changePauseStrings.getPauseTitle(autoFillFormSource)
        val pauseSubtitle = changePauseStrings.getNotPausedMessage(autoFillFormSource)

        return ChangePauseModel(
            processing,
            autoFillFormSource,
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
