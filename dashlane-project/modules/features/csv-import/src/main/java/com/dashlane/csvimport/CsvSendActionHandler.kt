package com.dashlane.csvimport

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.dashlane.csvimport.csvimport.view.CsvImportActivity
import com.dashlane.navigation.NavigationHelper.Destination
import com.dashlane.navigation.NavigationUriBuilder
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.coroutines.getDeferredViewModel
import com.dashlane.util.getParcelableExtraCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class CsvSendActionHandler : DashlaneActivity() {
    override var requireUserUnlock = false

    @Inject
    lateinit var sessionManager: SessionManager

    private val hasSession: Boolean by lazy {
        sessionManager.session != null
    }

    private val uri: Uri
        get() = requireNotNull(
            intent?.takeIf { it.action == Intent.ACTION_SEND }
            ?.getParcelableExtraCompat(Intent.EXTRA_STREAM)
        ) { "uri == null" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch(Dispatchers.Main.immediate) {
            runCatching {
                val intent = if (hasSession) {
                    CsvImportActivity.newIntent(
                        this@CsvSendActionHandler,
                        uri
                    )
                } else {
                    Intent(Intent.ACTION_VIEW).apply {
                        data = NavigationUriBuilder()
                            .host(Destination.MainPath.CSV_IMPORT)
                            .appendQueryParameter(Destination.PathQueryParameters.CsvImport.URI, copiedUri().toString())
                            .build()
                    }
                }

                startActivity(intent)
            }

            finish()
        }
    }

    private suspend fun copiedUri(): Uri {
        val copyViewModel = ViewModelProvider(this)
            .getDeferredViewModel<Uri>("copy")

        val deferred = copyViewModel.deferred ?: copyViewModel.async(Dispatchers.IO) {
            val file = File.createTempFile("csv_send_action_handler", null, cacheDir)

            requireNotNull(contentResolver.openInputStream(uri)) { "cannot open uri" }
                .use { input -> file.outputStream().use { output -> input.copyTo(output) } }

            Uri.fromFile(file)
        }

        return deferred.await()
    }
}