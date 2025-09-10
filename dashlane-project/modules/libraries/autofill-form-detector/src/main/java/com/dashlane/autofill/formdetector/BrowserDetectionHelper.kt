package com.dashlane.autofill.formdetector

object BrowserDetectionHelper {
    private val URL_BAR_ID_BY_PACKAGE: Map<String, String> = mapOf(
        "com.android.chrome" to "url_bar",
        "com.chrome.beta" to "url_bar",
        "com.chrome.dev" to "url_bar",
        "com.chrome.canary" to "url_bar",
        "com.duckduckgo.mobile.android" to "omnibarTextInput",
        "com.ecosia.android" to "url_bar",
        "com.kiwibrowser.browser" to "url_bar",
        "com.microsoft.emmx" to "url_bar",
        "com.sec.android.app.sbrowser" to "location_bar_edit_text",
        "com.sec.android.app.sbrowser.beta" to "location_bar_edit_text",
        "com.opera.browser" to "url_field",
        "com.opera.mini.native" to "url_field",
        "com.opera.browser.beta" to "url_field",
        "com.opera.mini.native.beta" to "url_field",
        "com.opera.touch" to "addressbarEdit",
        "org.mozilla.firefox" to "mozac_browser_toolbar_url_view",
        "org.mozilla.firefox_beta" to "mozac_browser_toolbar_url_view",
        "org.mozilla.fenix" to "mozac_browser_toolbar_url_view",
        "org.mozilla.fennec_aurora" to "mozac_browser_toolbar_url_view",
        "org.mozilla.focus" to "display_url",
        "com.brave.browser" to "url_bar",
        "com.brave.browser_beta" to "url_bar",
        "com.brave.browser_nightly" to "url_bar",
        "com.yandex.browser" to "bro_omnibar_address_title_text",
        "com.yandex.browser.alpha" to "bro_omnibar_address_title_text",
        "com.vivaldi.browser" to "url_bar",
        "com.vivaldi.browser.snapshot" to "url_bar",
        "com.vivaldi.browser.sopranos" to "url_bar",
    )

    fun isBrowserSupported(packageName: String) =
        URL_BAR_ID_BY_PACKAGE.containsKey(packageName)

    fun isUrlBar(packageName: String, resourceName: String): Boolean {
        return URL_BAR_ID_BY_PACKAGE[packageName]
            ?.let { it == resourceName || resourceName.endsWith("/$it") }
            ?: false
    }
}