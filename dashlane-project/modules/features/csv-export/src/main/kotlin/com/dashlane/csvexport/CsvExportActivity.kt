package com.dashlane.csvexport

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.navigation.Navigator
import com.dashlane.ui.common.compose.components.LoadingScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CsvExportActivity : AppCompatActivity() {
    @Inject
    lateinit var navigator: Navigator

    private val viewModel by viewModels<CSVExportViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                LaunchedEffect(viewModel.stateFlow) {
                    viewModel.stateFlow.sideEffect
                        .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                        .collect { sideEffect ->
                            when (sideEffect) {
                                is CSVExportNavigationEffect.Success -> {
                                    setResult(RESULT_CODE_SUCCESS)
                                    finish()
                                }
                                is CSVExportNavigationEffect.Failure -> {
                                    setResult(RESULT_CODE_FAILURE)
                                    finish()
                                }
                                is CSVExportNavigationEffect.FailureShare -> {
                                    share(sideEffect.files)
                                }
                                is CSVExportNavigationEffect.OpenLink -> {
                                    navigator.goToWebView(sideEffect.link)
                                }
                            }
                        }
                }

                val viewState by viewModel.stateFlow.viewState.collectAsStateWithLifecycle()

                Crossfade(targetState = viewState, label = "csvExport") { state ->
                    if (state.isLoading) {
                        LoadingScreen(title = stringResource(R.string.csv_export_loading))
                    } else {
                        CSVExportIntroScreen(
                            onClose = ::finish,
                            onClickPositiveButton = viewModel::export,
                            onClickExternalLink = viewModel::openLink,
                        )
                    }
                }
            }
        }
    }

    private fun share(files: List<Uri>) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(files))
            type = "\1/*"
        }
        startActivity(Intent.createChooser(shareIntent, null))
    }

    companion object {
        const val REQUEST_CODE = 1900
        const val RESULT_CODE_SUCCESS = 1901
        const val RESULT_CODE_FAILURE = 1902
    }
}