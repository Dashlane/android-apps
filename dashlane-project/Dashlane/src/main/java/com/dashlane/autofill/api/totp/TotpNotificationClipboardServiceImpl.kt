package com.dashlane.autofill.api.totp

import android.content.ClipData
import android.content.ClipboardManager
import com.dashlane.autofill.api.totp.services.TotpNotificationClipboardService
import javax.inject.Inject
import javax.inject.Provider

internal class TotpNotificationClipboardServiceImpl @Inject constructor(
    private val clipboardManagerProvider: Provider<ClipboardManager>
) : TotpNotificationClipboardService {

    companion object {
        private const val CLIP_CANON_DESCRIPTION = "clip for totp: "
    }

    private val clipboardManager
        get() = clipboardManagerProvider.get()

    override fun copy(totpNotificationId: String?, code: String) {
        val clipCanonDescription = buildClipCanonDescription(totpNotificationId)
        try {
            val clip = ClipData.newPlainText(clipCanonDescription, code)
            if (clip != null) {
                clipboardManager.setPrimaryClip(clip)
            }
        } catch (e: Exception) {
        }
    }

    private fun buildClipCanonDescription(totpNotificationId: String?): String {
        return CLIP_CANON_DESCRIPTION + totpNotificationId
    }
}
