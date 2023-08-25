package com.dashlane.vault.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class DeviceType(
    @StringRes
    val nameResId: Int,
    @DrawableRes
    val iconResId: Int,
    val isDesktop: Boolean,
    val code: String,
    vararg tags: String
) {
    WINDOWS(
        nameResId = R.string.device_windows,
        iconResId = R.drawable.device_windows,
        isDesktop = true,
        code = "server_win"
    ),
    MAC(
        nameResId = R.string.device_mac,
        iconResId = R.drawable.device_apple,
        isDesktop = true,
        code = "server_macos"
    ),
    ANDROID(
        nameResId = R.string.device_android,
        iconResId = R.drawable.device_android,
        isDesktop = false,
        code = "server_android",
        "android"
    ),
    IPHONE(
        nameResId = R.string.device_iphone,
        iconResId = R.drawable.device_apple,
        isDesktop = false,
        code = "server_iphone",
        "server_ipod"
    ),
    IPAD(
        nameResId = R.string.device_ipad,
        iconResId = R.drawable.device_apple,
        isDesktop = false,
        code = "server_ipad"
    ),
    STANDALONE(
        nameResId = R.string.device_standalone,
        iconResId = R.drawable.device_browser,
        isDesktop = false,
        code = "server_standalone"
    ),
    WEB_APP(
        nameResId = R.string.device_web_app,
        iconResId = R.drawable.device_browser,
        isDesktop = false,
        code = "server_leeloo",
        "server_leeloo_dev",
        "server_wac",
        "website",
        "real_website"
    ),
    UNKNOWN(R.string.device_unknow, R.drawable.device_unknown, false, "");

    val tags = setOf(code) + tags

    companion object {

        @JvmStatic
        fun forValue(value: String?): DeviceType =
            enumValues<DeviceType>().firstOrNull { it.tags.contains(value) } ?: UNKNOWN
    }
}
