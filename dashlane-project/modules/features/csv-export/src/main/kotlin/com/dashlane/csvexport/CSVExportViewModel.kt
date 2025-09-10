package com.dashlane.csvexport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.csvexport.exporter.CSVExporter
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.BackupFileType
import com.dashlane.hermes.generated.definitions.TransferDataSource
import com.dashlane.hermes.generated.definitions.TransferDataStatus
import com.dashlane.hermes.generated.definitions.TransferDataStep
import com.dashlane.hermes.generated.events.user.ExportData
import com.dashlane.mvvm.MutableViewStateFlow
import com.dashlane.mvvm.ViewStateFlow
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CSVExportViewModel @Inject constructor(
    private val csvExporter: CSVExporter,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val logRepository: LogRepository,
) : ViewModel() {
    private val _stateFlow =
        MutableViewStateFlow<CSVExportViewState, CSVExportNavigationEffect>(CSVExportViewState(isLoading = false))
    val stateFlow: ViewStateFlow<CSVExportViewState, CSVExportNavigationEffect> = _stateFlow

    fun export() {
        viewModelScope.launch(ioDispatcher) {
            logExportStarted()
            _stateFlow.update { state -> state.copy(isLoading = true) }
            runCatching { csvExporter.export() }
                .onSuccess {
                    logExportSuccess()
                    _stateFlow.send(CSVExportNavigationEffect.Success)
                }
                .onFailure {
                    if (it is CSVExporter.FallbackException) {
                        _stateFlow.update { state -> state.copy(isLoading = false) }
                        _stateFlow.send(CSVExportNavigationEffect.FailureShare(it.files))
                    } else {
                        _stateFlow.send(CSVExportNavigationEffect.Failure)
                    }
                }
        }
    }

    fun logExportStarted() {
        logRepository.queueEvent(
            ExportData(
                backupFileType = BackupFileType.CSV,
                exportDataStatus = TransferDataStatus.START,
                exportDataStep = TransferDataStep.SELECT_EXPORT_DESTINATION,
                exportDestination = TransferDataSource.SOURCE_OTHER,
            ),
        )
    }

    fun logExportSuccess() {
        logRepository.queueEvent(
            ExportData(
                exportDataStatus = TransferDataStatus.SUCCESS,
                exportDestination = TransferDataSource.SOURCE_OTHER,
                exportDataStep = TransferDataStep.SUCCESS,
                backupFileType = BackupFileType.CSV,
            ),
        )
    }

    fun openLink(link: String) {
        viewModelScope.launch(ioDispatcher) {
            _stateFlow.send(CSVExportNavigationEffect.OpenLink(link))
        }
    }
}