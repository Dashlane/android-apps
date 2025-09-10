package com.dashlane.csvimport.intro

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dashlane.csvimport.R
import com.dashlane.csvimport.csvimport.view.CsvImportActivity
import com.dashlane.csvimport.utils.Intents
import com.dashlane.csvimport.utils.localBroadcastManager
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.help.HelpCenterCoordinator
import com.dashlane.help.HelpCenterLink
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BackupFileType
import com.dashlane.hermes.generated.definitions.TransferDataSource
import com.dashlane.hermes.generated.definitions.TransferDataStatus
import com.dashlane.hermes.generated.definitions.TransferDataStep
import com.dashlane.hermes.generated.events.user.ImportData
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.NavigationUriBuilder
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CsvFileImportIntroActivity : DashlaneActivity() {
    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var logRepository: LogRepository

    private val csvImportReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            
            
            
            when (intent.getStringExtra(Intents.EXTRA_CSV_IMPORT_RESULT)) {
                Intents.CSV_IMPORT_RESULT_SUCCESS -> {
                    startActivity(
                        Intent(Intent.ACTION_VIEW)
                            .apply {
                                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                data = NavigationUriBuilder()
                                    .host(NavigationHelper.Destination.MainPath.PASSWORDS)
                                    .build()
                            },
                    )
                }

                Intents.CSV_IMPORT_RESULT_FAILURE -> {
                    showErrorDialog()
                }

                Intents.CSV_IMPORT_RESULT_CANCEL -> {
                    finish()
                }

                Intents.CSV_IMPORT_RESULT_ADD_INDIVIDUALLY -> {
                    navigator.goToCredentialAddStep1()
                    finish()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val fromCompetitor = intent.getBooleanExtra(EXTRA_FROM_COMPETITOR, false)
        setContent {
            DashlaneTheme {
                CsvFileImportIntroScreen(
                    fromCompetitor = fromCompetitor,
                    goToCsvImport = ::goToCsvImport,
                    onCancelClick = ::onCancelClick,
                    onSelectFileClicked = ::logImportStarted,
                )
            }
        }

        val page = if (fromCompetitor) AnyPage.IMPORT_PASSWORD_MANAGER else AnyPage.IMPORT_CSV
        setCurrentPageView(page)

        localBroadcastManager.registerReceiver(csvImportReceiver, IntentFilter(Intents.ACTION_CSV_IMPORT))
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { localBroadcastManager.unregisterReceiver(csvImportReceiver) }
    }

    private fun showErrorDialog() {
        DialogHelper().builder(this)
            .setTitle(R.string.csv_file_import_error_title)
            .setMessage(R.string.csv_file_import_error_description)
            .setPositiveButton(R.string.csv_file_import_error_positive) { _, _ ->
                openCsvImportHelpCenter()
            }
            .setNegativeButton(R.string.csv_file_import_error_negative) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun openCsvImportHelpCenter() {
        HelpCenterCoordinator.openLink(this, HelpCenterLink.ARTICLE_CSV_IMPORT)
    }

    private fun goToCsvImport(uri: Uri) {
        startActivity(CsvImportActivity.newIntent(this, uri))
    }

    private fun onCancelClick() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun logImportStarted() {
        
        logRepository.queueEvent(
            ImportData(
                backupFileType = BackupFileType.CSV,
                importDataStatus = TransferDataStatus.START,
                importSource = TransferDataSource.SOURCE_OTHER,
                importDataStep = TransferDataStep.SELECT_FILE,
                isDirectImport = false,
            ),
        )
    }

    companion object {
        private const val EXTRA_FROM_COMPETITOR = "fromCompetitor" 
    }
}