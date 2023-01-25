package com.dashlane.util

import android.net.Uri
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.navigation.SchemeUtils
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDataRepository
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.useractivity.log.install.InstallLogCode67
import com.dashlane.useractivity.log.install.InstallLogRepository



class DeepLinkLogger(
    private val sessionManager: SessionManager,
    private val installLogRepository: InstallLogRepository,
    private val userDataRepository: UserDataRepository,
    private val genericDataQuery: GenericDataQuery
) {

    

    fun log(uri: Uri, status: String) {
        SingletonProvider.getThreadHelper().runOnBackgroundThread {
            val itemId = SchemeUtils.getItemId(uri.schemeSpecificPart)
            val anonymousItemId = itemId?.let {
                genericDataQuery.queryFirst(GenericFilter("{$it}", null))?.anonymousId
            }
            val cleanUri = uri.schemeSpecificPart.let {
                if (itemId.isNullOrBlank()) {
                    it
                } else {
                    it.replace(itemId, anonymousItemId ?: "")
                }
            }
            installLogRepository.enqueue(
                InstallLogCode67(
                    origin = uri.getQueryParameter("origin"),
                    status = status,
                    userid = sessionManager.session?.let { userDataRepository.getSettingsManager(it) }
                        ?.getSettings()?.anonymousUserId,
                    url = cleanUri
                )
            )
        }
    }

    companion object {

        operator fun invoke(): DeepLinkLogger {
            return DeepLinkLogger(
                SingletonProvider.getSessionManager(),
                SingletonProvider.getComponent().installLogRepository,
                SingletonProvider.getComponent().userDataRepository,
                SingletonProvider.getMainDataAccessor().getGenericDataQuery()
            )
        }
    }
}