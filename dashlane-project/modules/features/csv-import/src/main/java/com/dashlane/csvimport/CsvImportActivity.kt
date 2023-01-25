package com.dashlane.csvimport

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dashlane.autofill.LinkedServicesHelper
import com.dashlane.csvimport.internal.csvimport.CsvImportContract
import com.dashlane.csvimport.internal.csvimport.CsvImportDataProvider
import com.dashlane.csvimport.internal.csvimport.CsvImportPresenter
import com.dashlane.csvimport.internal.csvimport.CsvImportViewProxy
import com.dashlane.hermes.inject.HermesComponent
import com.dashlane.security.DashlaneIntent
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.dashlane.useractivity.log.usage.UsageLogCode75
import com.dashlane.util.ActivityResultContractCompat
import com.dashlane.util.getParcelableCompat
import com.dashlane.util.getParcelableExtraCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import javax.inject.Inject

@AndroidEntryPoint
class CsvImportActivity : DashlaneActivity() {

    @Inject
    lateinit var csvImportViewTypeProviderFactory: CsvImportViewTypeProvider.Factory

    @Inject
    lateinit var importAuthentifiantHelper: ImportAuthentifiantHelper

    @Inject
    lateinit var mainDataAccessor: MainDataAccessor

    @Inject
    lateinit var linkedServicesHelper: LinkedServicesHelper

    private lateinit var presenter: CsvImportContract.Presenter
    private lateinit var provider: CsvImportContract.DataProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent.getParcelableExtraCompat<Uri>(EXTRA_URI)
        if (uri == null) {
            finish()
            return
        }

        setContentView(R.layout.activity_csv_import)

        val origin = intent.getStringExtra(EXTRA_ORIGIN) ?: ""

        provider = ViewModelProvider(
            this,
            DataProviderHolder.Factory(
                application,
                uri,
                savedInstanceState?.getParcelableCompat(KEY_STATE) ?: CsvImportContract.State.Initial,
                importAuthentifiantHelper,
                mainDataAccessor,
                linkedServicesHelper
            )
        )[DataProviderHolder::class.java].provider

        val usageLogRepository = UserActivityComponent(this).currentSessionUsageLogRepository
        val logRepository = HermesComponent(this).logRepository
        val customCsvImportActivityResultLauncher = registerForActivityResult(
            object : ActivityResultContractCompat<List<String>>() {
                override fun createIntent(context: Context, input: List<String>): Intent =
                    CustomCsvImportActivity.newIntent(context, input, origin)
            }
        ) { (resultCode, intent) ->
            onCustomCsvImportActivityResult(resultCode, intent)
        }
        presenter = CsvImportPresenter(
            lifecycleScope,
            customCsvImportActivityResultLauncher,
            logRepository,
            usageLogRepository,
            origin
        ).apply {
            setView(
                CsvImportViewProxy(
                    this@CsvImportActivity,
                    csvImportViewTypeProviderFactory,
                    importAuthentifiantHelper
                )
            )
            setProvider(provider)
            this.onCreate(savedInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(KEY_STATE, provider.currentState)
    }

    private fun onCustomCsvImportActivityResult(resultCode: Int, data: Intent?) {
        presenter.onCustomCsvImportActivityResult(resultCode, data)
    }

    private class DataProviderHolder(
        application: Application,
        uri: Uri,
        currentState: CsvImportContract.State,
        importAuthentifiantHelper: ImportAuthentifiantHelper,
        mainDataAccessor: MainDataAccessor,
        linkedServicesHelper: LinkedServicesHelper
    ) : ViewModel(), CoroutineScope by MainScope() {
        class Factory(
            private val application: Application,
            private val uri: Uri,
            private val currentState: CsvImportContract.State,
            private val importAuthentifiantHelper: ImportAuthentifiantHelper,
            private val mainDataAccessor: MainDataAccessor,
            private val linkedServicesHelper: LinkedServicesHelper
        ) : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                check(modelClass == DataProviderHolder::class.java)
                @Suppress("UNCHECKED_CAST")
                return DataProviderHolder(
                    application,
                    uri,
                    currentState,
                    importAuthentifiantHelper,
                    mainDataAccessor,
                    linkedServicesHelper
                ) as T
            }
        }

        val provider = CsvImportDataProvider(
            currentState,
            application,
            this,
            mainDataAccessor,
            importAuthentifiantHelper,
            linkedServicesHelper
        ) { application.contentResolver.openInputStream(uri)!! }

        override fun onCleared() = cancel()
    }

    companion object {
        private const val EXTRA_URI = "uri"
        private const val EXTRA_ORIGIN = "origin"
        private const val KEY_STATE = "state"

        @JvmStatic
        fun newIntent(
            context: Context,
            uri: Uri,
            origin: UsageLogCode75.Origin?
        ): Intent = newIntent(context, uri, origin?.code)

        @JvmStatic
        fun newIntent(
            context: Context,
            uri: Uri,
            origin: String?
        ): Intent = DashlaneIntent.newInstance(context, CsvImportActivity::class.java)
            .putExtra(EXTRA_URI, uri)
            .putExtra(EXTRA_ORIGIN, origin)
    }
}