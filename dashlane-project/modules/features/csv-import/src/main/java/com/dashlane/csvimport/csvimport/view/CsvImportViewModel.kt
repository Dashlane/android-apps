package com.dashlane.csvimport.csvimport.view

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.csvimport.csvimport.CsvImportManager
import com.dashlane.csvimport.csvimport.CsvSchema
import com.dashlane.csvimport.csvimport.ImportAuthentifiantHelper
import com.dashlane.csvimport.csvimport.toVaultItem
import com.dashlane.csvimport.utils.Intents
import com.dashlane.csvimport.utils.Intents.CSV_IMPORT_RESULT_ADD_INDIVIDUALLY
import com.dashlane.csvimport.utils.Intents.CSV_IMPORT_RESULT_CANCEL
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.BackupFileType
import com.dashlane.hermes.generated.definitions.ImportDataStatus
import com.dashlane.hermes.generated.definitions.ImportDataStep
import com.dashlane.hermes.generated.definitions.ImportSource
import com.dashlane.hermes.generated.events.user.ImportData
import com.dashlane.util.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CsvImportViewModel @Inject constructor(
    private val logRepository: LogRepository,
    private val manager: CsvImportManager,
    private val authentifiantHelper: ImportAuthentifiantHelper,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val uri: Uri = savedStateHandle[CsvImportActivity.EXTRA_URI]!!

    private val stateFlow =
        MutableStateFlow<CsvImportState>(CsvImportState.Initial(CsvImportStateData(listOf())))
    val uiState = stateFlow.asStateFlow()

    init {
        manager.setFileUri(uri)
        loadCredentials()
    }

    fun onPrimaryCtaClicked() {
        val hasItems = stateFlow.value.data.selectedCredentials.isNotEmpty()
        if (hasItems) {
            
            saveCredentials()
        } else {
            
            stateFlow.tryEmit(CsvImportState.Error(stateFlow.value.data, CSV_IMPORT_RESULT_CANCEL))
        }
    }

    fun onSecondaryCtaClicked() {
        val hasItems = stateFlow.value.data.selectedCredentials.isNotEmpty()
        if (hasItems) {
            
            stateFlow.tryEmit(CsvImportState.Error(stateFlow.value.data, CSV_IMPORT_RESULT_CANCEL))
        } else {
            
            stateFlow.tryEmit(CsvImportState.Error(stateFlow.value.data, CSV_IMPORT_RESULT_ADD_INDIVIDUALLY))
        }
    }

    fun matchFields(types: List<CsvSchema.FieldType?>) {
        val state = stateFlow.value as? CsvImportState.Matching ?: return
        val schema = CsvSchema(
            hasHeader = false,
            separator = state.separator,
            fieldTypes = types
        )
        stateFlow.tryEmit(CsvImportState.Loading(stateFlow.value.data))
        viewModelScope.launch(ioDispatcher) {
            try {
                val credentialsFromSchema = manager.loadCredentials(schema)
                stateFlow.emit(
                    CsvImportState.Loaded(
                        stateFlow.value.data.copy(selectedCredentials = credentialsFromSchema),
                        false
                    )
                )
            } catch (ex: Exception) {
                stateFlow.emit(CsvImportState.Error(stateFlow.value.data, Intents.CSV_IMPORT_RESULT_FAILURE))
            }
        }
    }

    fun toggleAuthentifiantSelection(position: Int) {
        val state = stateFlow.value

        if (state !is CsvImportState.Loaded || state.saving) return

        val credentials = state.data.selectedCredentials.mapIndexed { index, csvAuthentifiant ->
            if (index == position) {
                csvAuthentifiant.copy(selected = !csvAuthentifiant.selected)
            } else {
                csvAuthentifiant
            }
        }
        stateFlow.tryEmit(state.copy(data = state.data.copy(selectedCredentials = credentials)))
    }

    fun matchActivityOpen(separator: Char) {
        stateFlow.tryEmit(CsvImportState.Matching(stateFlow.value.data, separator))
    }

    private fun loadCredentials() {
        if (stateFlow.value !is CsvImportState.Initial) return

        stateFlow.tryEmit(CsvImportState.Loading(stateFlow.value.data))

        viewModelScope.launch(ioDispatcher) {
            try {
                val schema = manager.inferSchema()
                if (schema == null) {
                    val (separator, fields) = manager.selectFields()!!
                    stateFlow.emit(
                        CsvImportState.OpenMatchActivity(stateFlow.value.data, fields, separator)
                    )
                } else {
                    stateFlow.emit(
                        CsvImportState.Loaded(
                            stateFlow.value.data.copy(selectedCredentials = manager.loadCredentials(schema)),
                            false
                        )
                    )
                }
            } catch (ex: Exception) {
                stateFlow.emit(CsvImportState.Error(stateFlow.value.data, Intents.CSV_IMPORT_RESULT_FAILURE))
            }
        }
    }

    private fun saveCredentials() {
        val state = stateFlow.value

        if (state !is CsvImportState.Loaded || state.saving) return

        viewModelScope.launch(ioDispatcher) {
            stateFlow.emit(state.copy(saving = true))
            try {
                val toSaveItems = state.data.selectedCredentials
                    .filter { it.selected }
                    .map { it.toVaultItem(authentifiantHelper) }
                val countSaved = authentifiantHelper.addAuthentifiants(
                    toSaveItems
                )
                logRepository.queueEvent(
                    ImportData(
                        backupFileType = BackupFileType.CSV,
                        importDataStatus = ImportDataStatus.SUCCESS,
                        importSource = ImportSource.SOURCE_OTHER,
                        importDataStep = ImportDataStep.SUCCESS,
                        isDirectImport = false
                    )
                )
                stateFlow.emit(CsvImportState.Saved(stateFlow.value.data, countSaved))
            } catch (ex: Exception) {
                stateFlow.emit(state.copy(saving = false))
            }
        }
    }
}