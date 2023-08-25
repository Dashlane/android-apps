package com.dashlane.csvimport.internal.csvimport

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.dashlane.csvimport.CustomCsvImportActivity
import com.dashlane.csvimport.ImportFromChromeLogger
import com.dashlane.csvimport.R
import com.dashlane.csvimport.internal.CsvSchema
import com.dashlane.csvimport.internal.ImportMultiplePasswordsLogger
import com.dashlane.csvimport.internal.Intents
import com.dashlane.csvimport.internal.Intents.ACTION_CSV_IMPORT
import com.dashlane.csvimport.internal.Intents.CSV_IMPORT_RESULT_ADD_INDIVIDUALLY
import com.dashlane.csvimport.internal.Intents.CSV_IMPORT_RESULT_CANCEL
import com.dashlane.csvimport.internal.Intents.CSV_IMPORT_RESULT_SUCCESS
import com.dashlane.csvimport.internal.Intents.EXTRA_CSV_IMPORT_RESULT
import com.dashlane.csvimport.internal.csvimport.CsvImportContract.State
import com.dashlane.csvimport.internal.localBroadcastManager
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.BackupFileType
import com.dashlane.hermes.generated.definitions.ImportDataStatus
import com.dashlane.hermes.generated.definitions.ImportDataStep
import com.dashlane.hermes.generated.definitions.ImportSource
import com.dashlane.hermes.generated.events.user.ImportData
import com.dashlane.useractivity.log.usage.UsageLogRepository
import com.dashlane.util.showToaster
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class CsvImportPresenter(
    private val coroutineScope: CoroutineScope,
    private val customCsvImportActivityResultLauncher: ActivityResultLauncher<List<String>>,
    private val logRepository: LogRepository,
    usageLogRepository: UsageLogRepository?,
    origin: String
) : BasePresenter<CsvImportContract.DataProvider, CsvImportContract.ViewProxy>(),
    CsvImportContract.Presenter {
    private var hasItems: Boolean? = null

    private val importFromChromeLogger = ImportFromChromeLogger(usageLogRepository, originStr = origin)
    private val importMultiplePasswordsLogger = ImportMultiplePasswordsLogger(usageLogRepository, originStr = origin)

    override fun onCreate(savedInstanceState: Bundle?) {
        coroutineScope.launch(Dispatchers.Main) {
            for (state in provider.states) {
                handleState(state)
            }
        }

        if (savedInstanceState == null) {
            importFromChromeLogger.logCsvImportDisplayed()
            importMultiplePasswordsLogger.logCsvImportDisplayed()
            provider.loadAuthentifiants()
        }
    }

    override fun onCustomCsvImportActivityResult(resultCode: Int, data: Intent?) {
        val fieldTypes = data?.getIntegerArrayListExtra(CustomCsvImportActivity.EXTRA_CATEGORIES)
            ?.map {
                when (it) {
                    CustomCsvImportActivity.CATEGORY_URL -> CsvSchema.FieldType.URL
                    CustomCsvImportActivity.CATEGORY_USERNAME -> CsvSchema.FieldType.USERNAME
                    CustomCsvImportActivity.CATEGORY_PASSWORD -> CsvSchema.FieldType.PASSWORD
                    else -> null
                }
            }

        if (resultCode == Activity.RESULT_OK && fieldTypes != null) {
            provider.matchFields(fieldTypes)
        } else {
            broadcastResultAndFinish(Intents.CSV_IMPORT_RESULT_FAILURE)
        }
    }

    override fun startMatchingFields(fields: List<String>) {
        customCsvImportActivityResultLauncher.launch(fields)
    }

    override fun onPrimaryCtaClicked() {
        val hasItems = hasItems ?: return

        if (hasItems) {
            
            importFromChromeLogger.logCsvImportImportAllClicked()
            importMultiplePasswordsLogger.logCsvImportAllClicked()
            provider.saveAuthentifiants()
        } else {
            
            importFromChromeLogger.logCsvImportCancelClicked()
            broadcastResultAndFinish(CSV_IMPORT_RESULT_CANCEL)
        }
    }

    override fun onSecondaryCtaClicked() {
        val hasItems = hasItems ?: return

        if (hasItems) {
            
            importFromChromeLogger.logCsvImportCancelClicked()
            importMultiplePasswordsLogger.logCsvImporCancelClicked()
            broadcastResultAndFinish(CSV_IMPORT_RESULT_CANCEL)
        } else {
            
            importFromChromeLogger.logCsvImportAddManuallyClicked()
            broadcastResultAndFinish(CSV_IMPORT_RESULT_ADD_INDIVIDUALLY)
        }
    }

    override fun toggleAuthentifiantSelection(position: Int) {
        provider.toggleAuthentifiantSelection(position)
    }

    private fun handleState(state: State) {
        when (state) {
            State.Loading -> view.showLoading()
            is State.Loaded -> {
                val (authentifiants, saving) = state

                if (saving) {
                    view.showLoading()
                    return
                }
                importFromChromeLogger.logCsvImportParseResult(authentifiants.size)
                importMultiplePasswordsLogger.logCsvImportResults(authentifiants.size)

                if (authentifiants.isEmpty()) {
                    hasItems = false
                    view.showEmptyState()
                } else {
                    hasItems = true
                    view.showList(authentifiants)
                }
            }
            is State.LoadingError -> {
                importFromChromeLogger.logCsvImportParseResult()
                importMultiplePasswordsLogger.logCsvImportResults()

                broadcastResultAndFinish(Intents.CSV_IMPORT_RESULT_FAILURE)
            }
            is State.Saved -> context?.run {
                this.showToaster(
                    resources.getQuantityString(
                        R.plurals.csv_import_success_message,
                        state.count,
                        state.count
                    ),
                    Toast.LENGTH_SHORT
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
                broadcastResultAndFinish(CSV_IMPORT_RESULT_SUCCESS)
            }
            else -> {}
        }
    }

    private fun broadcastResultAndFinish(@Intents.CsvImportResult result: String) {
        val csvImportIntent = Intent(ACTION_CSV_IMPORT)
            .putExtra(EXTRA_CSV_IMPORT_RESULT, result)

        activity?.run {
            localBroadcastManager.sendBroadcast(csvImportIntent)
            finish()
        }
    }
}