package com.dashlane.login

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.dashlane.util.showToaster
import kotlin.reflect.KMutableProperty0

object InstallationIdDebugUtil {
    private const val TEST_INPUT: String = "test_analytics_id"

    fun showInstallationId(
        context: Context?,
        text: KMutableProperty0<String>,
        installationIdProvider: () -> String
    ): Boolean {
        if (text.get() != TEST_INPUT) return false

        val installationId = installationIdProvider()
        text.set(installationId)
        context?.copyToClipboard(installationId)

        return true
    }

    private fun Context.copyToClipboard(installationId: String) {
        ContextCompat.getSystemService(this, ClipboardManager::class.java)?.let { clipboardManager ->
            val clip = ClipData.newPlainText("Installation Id", installationId)
            clipboardManager.setPrimaryClip(clip)
            showToaster("Copied to clipboard", Toast.LENGTH_SHORT)
        }
    }
}