package com.dashlane.braze

import com.braze.models.inappmessage.InAppMessageModal
import com.dashlane.braze.BrazeConstants.BRAZE_TRACKING_KEY_PARAM

data class BrazeInAppMessage(
    val imageUrl: String?,
    val title: String,
    val message: String,
    val extras: Map<String, String>,
    val id: String,
    val buttons: List<Button>,
    val trackingKey: String?,
    val onImpression: () -> Unit
) {

    var displayed = false

    fun logImpression() {
        displayed = true
        onImpression()
    }

    fun isDeviceExcluded(): Boolean {
        val excluded: List<String> = extras[EXTRA_DEVICE_EXCLUDE]?.split(",") ?: return false

        return excluded.contains(EXCLUDED_TABLET) || excluded.contains(EXCLUDED_PHONE)
    }

    data class Button(val text: String, val uri: String? = null, val onClick: () -> Unit = {})

    companion object {
        const val EXTRA_TRACKING_KEY = BRAZE_TRACKING_KEY_PARAM
        private const val EXTRA_DEVICE_EXCLUDE = "DEVICE_EXCLUDE"
        private const val EXCLUDED_PHONE = "ANDROID_PHONE"
        private const val EXCLUDED_TABLET = "ANDROID_TABLET"
    }
}

fun InAppMessageModal.toBrazeInAppMessage(): BrazeInAppMessage {
    return BrazeInAppMessage(
        imageUrl = imageUrl,
        title = header ?: "",
        message = message ?: "",
        extras = extras,
        id = triggerId ?: "",
        buttons = messageButtons.mapNotNull { messageButton ->
            val text = messageButton.text
            if (text != null) {
                BrazeInAppMessage.Button(text = text, uri = messageButton.uri.toString()) {
                    logButtonClick(messageButton)
                }
            } else {
                null
            }
        },
        trackingKey = extras[BrazeInAppMessage.EXTRA_TRACKING_KEY],
        onImpression = ::logImpression
    )
}
