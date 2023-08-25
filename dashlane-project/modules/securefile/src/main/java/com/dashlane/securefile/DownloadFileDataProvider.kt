package com.dashlane.securefile

import com.dashlane.securefile.extensions.getSecureFileInfo
import com.dashlane.securefile.extensions.toSecureFile
import com.dashlane.securefile.storage.DownloadAccessException
import com.dashlane.securefile.storage.SecureFileStorage
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.skocken.presentation.provider.BaseDataProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class DownloadFileDataProvider @Inject constructor(
    private val secureFileStorage: SecureFileStorage,
    private val mainDataAccessor: MainDataAccessor
) : BaseDataProvider<DownloadFileContract.Presenter>(), DownloadFileContract.DataProvider {

    @Suppress("EXPERIMENTAL_API_USAGE")
    override suspend fun downloadSecureFile(attachment: Attachment) {
        coroutineScope {
            val anonymousId = attachment.id?.let { mainDataAccessor.getVaultDataQuery().getSecureFileInfo(it)?.syncObject?.anonId }
            val progression =
                actor<Float> { consumeEach { presenter.notifyFileDownloadProgress(attachment, anonymousId, it) } }
            try {
                secureFileStorage.download(attachment.toSecureFile(), progression)
                presenter.notifyFileDownloaded(attachment, anonymousId)
            } catch (e: Exception) {
                when (e) {
                    is CancellationException -> Unit
                    is DownloadAccessException -> {
                        presenter.notifyFileAccessError(attachment, anonymousId)
                    }
                    else -> {
                        presenter.notifyFileDownloadError(attachment, anonymousId, e)
                    }
                }
            }
            progression.close()
        }
    }
}