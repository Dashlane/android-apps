package com.dashlane.braze

import com.braze.models.inappmessage.InAppMessageModal
import com.dashlane.braze.BrazeConstants.BRAZE_TRACKING_KEY_PARAM



class BrazeInAppMessage(private val inAppMessage: InAppMessageModal) {

    val imageUrl: String? = inAppMessage.imageUrl

    val title: String = inAppMessage.header ?: ""

    val message: String = inAppMessage.message ?: ""

    val extras: Map<String, String> = inAppMessage.extras

    val id: String = inAppMessage.triggerId ?: ""

    val buttons: List<Button> = inAppMessage.messageButtons.mapNotNull { messageButton ->
        val text = messageButton.text
        if (text != null) {
            Button(text = text, uri = messageButton.uri.toString()) {
                inAppMessage.logButtonClick(messageButton)
            }
        } else {
            null
        }
    }

    val trackingKey: String? = extras[EXTRA_TRACKING_KEY]

    var displayed = false

    fun logImpression() {
        displayed = true
        inAppMessage.logImpression()
    }

    fun isDeviceExcluded(): Boolean {
        val excluded: List<String> = extras[EXTRA_DEVICE_EXCLUDE]?.split(",") ?: return false

        return excluded.contains(EXCLUDED_TABLET) || excluded.contains(EXCLUDED_PHONE)
    }

    

    data class Button(val text: String, val uri: String? = null, val onClick: () -> Unit = {})

    companion object {
        private const val EXTRA_TRACKING_KEY = BRAZE_TRACKING_KEY_PARAM
        private const val EXTRA_DEVICE_EXCLUDE = "DEVICE_EXCLUDE"
        private const val EXCLUDED_PHONE = "ANDROID_PHONE"
        private const val EXCLUDED_TABLET = "ANDROID_TABLET"
    }
}