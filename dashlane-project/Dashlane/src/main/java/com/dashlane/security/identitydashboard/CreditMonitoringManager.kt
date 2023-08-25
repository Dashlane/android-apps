package com.dashlane.security.identitydashboard

import com.dashlane.network.webservices.DashlaneUrls
import com.dashlane.session.SessionManager
import com.dashlane.util.tryOrNull
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class CreditMonitoringManager @Inject constructor(
    callFactory: okhttp3.Call.Factory,
    private val sessionManager: SessionManager
) {

    private val creditViewService = Retrofit.Builder()
        .callFactory(callFactory)
        .baseUrl(DashlaneUrls.URL_WEBSERVICES)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CreditMonitoringService::class.java)

    suspend fun getLink(): String? {
        val session = sessionManager.session ?: return ERROR_UNKNOWN
        val result = tryOrNull {
            creditViewService.getTransunionLink(session.userId, session.uki).content
        }
        return result?.url
    }

    companion object {
        const val ERROR_UNKNOWN = "UNKNOWN"
    }
}
