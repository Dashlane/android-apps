package com.dashlane.csvimport

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.launch
import com.dashlane.csvimport.internal.ImportMultiplePasswordsLogger
import com.dashlane.csvimport.internal.Intents
import com.dashlane.csvimport.internal.localBroadcastManager
import com.dashlane.help.HelpCenterCoordinator
import com.dashlane.help.HelpCenterLink
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.navigation.NavigationHelper
import com.dashlane.navigation.NavigationUriBuilder
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.ui.util.DialogHelper
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.util.ActivityResultContractCompat
import com.dashlane.util.setCurrentPageView
import com.skocken.presentation.presenter.BasePresenter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CsvFileImportIntroActivity : DashlaneActivity() {

    @Inject
    lateinit var helpCenterCoordinator: HelpCenterCoordinator

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
                            }
                    )
                }
                Intents.CSV_IMPORT_RESULT_FAILURE -> {
                    showErrorDialog()
                }
                Intents.CSV_IMPORT_RESULT_CANCEL -> {
                    finish()
                }
                Intents.CSV_IMPORT_RESULT_ADD_INDIVIDUALLY -> {
                    navigator.goToCredentialAddStep1(null)
                    finish()
                }
            }
        }
    }

    private val logger: ImportMultiplePasswordsLogger by lazy {
        val usageLogRepository = UserActivityComponent(this).currentSessionUsageLogRepository
        ImportMultiplePasswordsLogger(usageLogRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_intro)

        val page = if (intent.getBooleanExtra(EXTRA_FROM_COMPETITOR, false)) {
            AnyPage.IMPORT_PASSWORD_MANAGER
        } else {
            AnyPage.IMPORT_CSV
        }

        setCurrentPageView(page)

        val getContentResultLauncher = registerForActivityResult(object : ActivityResultContractCompat<Unit>() {
            override fun createIntent(context: Context, input: Unit): Intent =
                Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = "\1/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_LOCAL_ONLY, true)
                }
        }) { (resultCode, intent) ->
            onGetContentResult(resultCode, intent)
        }
        val view = IntroScreenViewProxy(this)
        Presenter(getContentResultLauncher, logger).setView(view)

        localBroadcastManager.registerReceiver(csvImportReceiver, IntentFilter(Intents.ACTION_CSV_IMPORT))

        if (savedInstanceState == null) {
            logger.logCsvFileImportDisplayed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { localBroadcastManager.unregisterReceiver(csvImportReceiver) }
    }

    private fun onGetContentResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            logger.logCsvFileImportFileSelected()
            val uri = data?.data!!
            startActivity(CsvImportActivity.newIntent(this, uri, UsageLogCode75.Origin.FROM_APP))
        }
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
        helpCenterCoordinator.openLink(this, HelpCenterLink.ARTICLE_CSV_IMPORT, false)
    }

    private class Presenter(
        private val getContentResultLauncher: ActivityResultLauncher<Unit>,
        private val logger: ImportMultiplePasswordsLogger
    ) :
        BasePresenter<IntroScreenContract.DataProvider, IntroScreenContract.ViewProxy>(),
        IntroScreenContract.Presenter {

        override fun onViewChanged() {
            super.onViewChanged()

            viewOrNull?.run {
                val fromCompetitor = activity?.intent?.getBooleanExtra(EXTRA_FROM_COMPETITOR, false)
                    ?: false

                val description = if (fromCompetitor) {
                    listOf(
                        R.string.csv_file_import_description_competitor_line1,
                        R.string.csv_file_import_description_competitor_line2,
                        R.string.csv_file_import_description_competitor_line3
                    )
                } else {
                    listOf(
                        R.string.csv_file_import_description_line1,
                        R.string.csv_file_import_description_line2,
                        R.string.csv_file_import_description_line3
                    )
                }.joinToString("\n") { context.getString(it) }

                setImageResource(if (fromCompetitor) R.drawable.ic_csv_file_import_competitor else R.drawable.ic_csv_file_import)
                setTitle(if (fromCompetitor) R.string.csv_file_import_title_competitor else R.string.csv_file_import_title)
                setDescription(description)
                setPositiveButton(if (fromCompetitor) R.string.csv_file_import_competitor_primary_cta else R.string.csv_file_import_primary_cta)
                setNegativeButton(if (fromCompetitor) R.string.csv_file_import_competitor_secondary_cta else R.string.csv_file_import_secondary_cta)
            }
        }

        override fun onClickPositiveButton() {
            getContentResultLauncher.launch()
        }

        override fun onClickNegativeButton() {
            logger.logCsvFileImportCancelClicked()

            activity?.run {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        }

        override fun onClickNeutralButton() = Unit

        override fun onClickLink(position: Int, label: Int) = Unit
    }

    companion object {
        private const val EXTRA_FROM_COMPETITOR = "fromCompetitor" 
    }
}