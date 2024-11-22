package com.dashlane.securefile

import com.dashlane.securefile.extensions.toSecureFile
import com.dashlane.securefile.storage.DownloadAccessException
import com.dashlane.securefile.storage.SecureFileStorage
import com.skocken.presentation.provider.BaseDataProvider
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope

class DownloadFileDataProvider @Inject constructor(
    private val secureFileStorage: SecureFileStorage
) : BaseDataProvider<DownloadFileContract.Presenter>(), DownloadFileContract.DataProvider {

    @Suppress("EXPERIMENTAL_API_USAGE")
    override suspend fun downloadSecureFile(attachment: Attachment) {
        coroutineScope {
            val progression =
                actor<Float> { consumeEach { presenter.notifyFileDownloadProgress(attachment, it) } }
            try {
                secureFileStorage.download(attachment.toSecureFile(), progression)
                presenter.notifyFileDownloaded(attachment)
            } catch (e: Exception) {
                when (e) {
                    is CancellationException -> Unit
                    is DownloadAccessException -> {
                        presenter.notifyFileAccessError(attachment)
                    }
                    else -> {
                        presenter.notifyFileDownloadError(attachment, e)
                    }
                }
            }
            progression.close()
        }
    }
}