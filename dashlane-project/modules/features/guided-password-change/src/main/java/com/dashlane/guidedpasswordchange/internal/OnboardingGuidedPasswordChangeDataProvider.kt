package com.dashlane.guidedpasswordchange.internal

import android.content.Context
import android.net.Uri
import android.view.autofill.AutofillManager
import androidx.core.net.toUri
import com.dashlane.url.toUrl
import com.dashlane.util.tryOrNull
import com.skocken.presentation.provider.BaseDataProvider
import okhttp3.OkHttpClient
import okhttp3.Request

internal class OnboardingGuidedPasswordChangeDataProvider :
    BaseDataProvider<OnboardingGuidedPasswordChangeContract.Presenter>(),
    OnboardingGuidedPasswordChangeContract.DataProvider {

    private val client = OkHttpClient()

    override suspend fun getPasswordChangeUrl(domain: String): Uri? {
        val changePasswordUrl = "${domain.toUrl()}/$WELL_KNOWN_PATH"
        val request = Request.Builder()
            .url(changePasswordUrl)
            .build()
        return tryOrNull {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) changePasswordUrl.toUri() else null
            }
        }
    }

    override fun isAutofillApiEnabled(context: Context): Boolean {
        val autoFillManager = context.getSystemService(AutofillManager::class.java)
        return autoFillManager.hasEnabledAutofillServices()
    }

    companion object {
        private const val WELL_KNOWN_PATH = ".well-known/change-password"
    }
}