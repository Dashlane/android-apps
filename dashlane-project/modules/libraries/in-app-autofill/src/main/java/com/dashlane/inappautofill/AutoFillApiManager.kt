package com.dashlane.inappautofill

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import android.provider.Settings
import android.view.autofill.AutofillManager
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.credentials.CredentialManager
import com.dashlane.util.tryOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AutoFillApiManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val autoFillManager: AutofillManager?
        get() = context.getSystemService(AutofillManager::class.java)

    fun getAutofillStatus() = when (tryOrNull { autoFillManager?.hasEnabledAutofillServices() }) {
        true -> AutofillStatus.ENABLED
        false -> AutofillStatus.DISABLED
        else -> AutofillStatus.UNKNOWN
    }

    fun createAutofillSettingsPendingIntent(enable: Boolean) =
        createAutofillSettingsIntent(context, if (enable) "com.dashlane" else "none")

    private fun createAutofillSettingsIntent(context: Context, packageName: String): PendingIntent {
        val autofillIntent = autofillSettingsIntent(packageName)
        val canOpenAutofillPage = autoFillManager?.isAutofillSupported == true &&
            autofillIntent.resolveActivity(context.packageManager) != null
        return when {
            
            canOpenAutofillPage -> autofillIntent.createSettingsPendingIntent()
            
            SDK_INT >= UPSIDE_DOWN_CAKE -> autoFillSettingsForAndroid14Intent()
            
            else -> Intent(Settings.ACTION_SETTINGS).createSettingsPendingIntent()
        }
    }

    private fun Intent.createSettingsPendingIntent() =
        PendingIntent.getActivity(context, 0, this, PendingIntent.FLAG_IMMUTABLE)

    @RequiresApi(34)
    private fun autoFillSettingsForAndroid14Intent() = CredentialManager.create(context).createSettingsPendingIntent()

    private fun autofillSettingsIntent(packageName: String) = Intent(
        Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE,
        "package: $packageName".toUri(),
    )

    enum class AutofillStatus {
        ENABLED,
        DISABLED,
        UNKNOWN,
    }
}