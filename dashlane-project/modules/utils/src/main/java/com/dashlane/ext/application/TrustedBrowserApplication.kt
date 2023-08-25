package com.dashlane.ext.application

import com.dashlane.core.helpers.AppSignature

object TrustedBrowserApplication {
    data class Browser(
        override val packageName: String,
        override val signatures: AppSignature?
    ) : KnownApplication {
        override val mainDomain: String? = null

        override fun isSecureToUse(url: String?): Boolean {
            return true
        }

        override fun canOpen(url: String?): Boolean {
            return false
        }
    }

    private val browsersList = listOf(
        browser(
            "com.android.chrome",
            KnownApplication.sha256("f0fd6c5b410f25cb25c3b53346c8972fae30f8ee7411df910480ad6b2d60db83")
        ),
        browser(
            "com.chrome.beta",
            KnownApplication.sha256("da633d34b69e63ae2103b49d53ce052fc5f7f3c53aab94fdc2a208bdfd14249c")
        ),
        browser(
            "com.chrome.canary",
            KnownApplication.sha256("2019dfa1fb23efbf70c5bcd1443c5beab04f3f2ff4366e9ac1e3457639a24cfc")
        ),
        browser(
            "com.chrome.dev",
            KnownApplication.sha256("9044ee5fee4bbc5e21dd44665431c4eb1f1f71a32716a0bc927bcbb39233cabf")
        ),

        browser(
            "com.microsoft.emmx",
            KnownApplication.sha256("01e1999710a82c2749b4d50c445dc85d670b6136089d0a766a73827c82a1eac9")
        ),
        browser(
            "com.microsoft.emmx.dev",
            KnownApplication.sha256("01e1999710a82c2749b4d50c445dc85d670b6136089d0a766a73827c82a1eac9")
        ), 
        browser(
            "com.microsoft.emmx.beta",
            KnownApplication.sha256("01e1999710a82c2749b4d50c445dc85d670b6136089d0a766a73827c82a1eac9")
        ), 
        browser(
            "com.microsoft.emmx.canary",
            KnownApplication.sha256("01e1999710a82c2749b4d50c445dc85d670b6136089d0a766a73827c82a1eac9")
        ), 

        browser(
            "com.opera.browser",
            KnownApplication.sha256("5d6afbf87f652af04647ada0df634cf22370900b164b09d50bd23aa2cb5285b8")
        ), 
        browser(
            "com.opera.browser.beta",
            KnownApplication.sha256("5d6afbf87f652af04647ada0df634cf22370900b164b09d50bd23aa2cb5285b8")
        ), 
        browser(
            "com.opera.mini.native",
            KnownApplication.sha256("57acbc525f1b2ebd19196cd6f014397cc910fd18841e0ae850febc3e1e593ff2")
        ),
        browser(
            "com.opera.mini.native.beta",
            KnownApplication.sha256("57acbc525f1b2ebd19196cd6f014397cc910fd18841e0ae850febc3e1e593ff2")
        ),
        browser(
            "com.opera.touch",
            KnownApplication.sha256("aad8e204d24d177934c9cd0c63cc6aa38efbf42c4a6957097e2210f57faa67aa")
        ),
        browser(
            "com.opera.gx",
            KnownApplication.sha256("aad8e204d24d177934c9cd0c63cc6aa38efbf42c4a6957097e2210f57faa67aa")
        ),

        browser(
            "com.sec.android.app.sbrowser",
            KnownApplication.sha256("34df0e7a9f1cf1892e45c056b4973cd81ccf148a4050d11aea4ac5a65f900a42")
        ),
        browser(
            "com.sec.android.app.sbrowser.beta",
            KnownApplication.sha256("34df0e7a9f1cf1892e45c056b4973cd81ccf148a4050d11aea4ac5a65f900a42")
        ), 

        browser(
            "org.mozilla.firefox",
            KnownApplication.sha256("a78b62a5165b4494b2fead9e76a280d22d937fee6251aece599446b2ea319b04")
        ),
        browser(
            "org.mozilla.firefox_beta",
            KnownApplication.sha256("a78b62a5165b4494b2fead9e76a280d22d937fee6251aece599446b2ea319b04")
        ),
        browser(
            "org.mozilla.fenix",
            KnownApplication.sha256("5004779088e7f988d5bc5cc5f8798febf4f8cd084a1b2a46efd4c8ee4aeaf211")
        ),
        browser(
            "org.mozilla.focus",
            KnownApplication.sha256("6203a473be36d64ee37f87fa500edbc79eab930610ab9b9fa4ca7d5c1f1b4ffc")
        ),

        browser(
            "com.brave.browser",
            KnownApplication.sha256("9c2db70513515fdbfbbc585b3edf3d7123d4dc67c94ffd306361c1d79bbf18ac")
        ),
        browser(
            "com.brave.browser_beta",
            KnownApplication.sha256("9c2db70513515fdbfbbc585b3edf3d7123d4dc67c94ffd306361c1d79bbf18ac")
        ),
        browser(
            "com.brave.browser_nightly",
            KnownApplication.sha256("9c2db70513515fdbfbbc585b3edf3d7123d4dc67c94ffd306361c1d79bbf18ac")
        ),

        browser(
            "com.duckduckgo.mobile.android",
            KnownApplication.sha256("bb7bb31c573c46a1da7fc5c528a6acf432108456feec50810c7f33694eb3d2d4")
        ),
        browser(
            "com.google.android.googlequicksearchbox",
            KnownApplication.sha512("edf99db872937471eb94cbe576512a0089527e28b5b65df96f18f539737955ef1ce2553a51156ee31b521dcdc1559c52e965899f13038487d03743742b634326")
        ),
        browser(
            "com.ecosia.android",
            KnownApplication.sha256("f522d5df0ab592168bdee6151413fd98022722f7f2f85efe72b81e28ddec765c")
        ),
        browser(
            "com.kiwibrowser.browser",
            KnownApplication.sha256("c069ea966332e91e0a0c3cc577e6f509fe3d9ddaf7015ad0c5c5ef8fda3f8628")
        ),
        browser(
            "com.yandex.browser",
            KnownApplication.sha256("aca405ded<8b25cb2e8c6da69425d2b4307d087c1276fc06ad5942731ccc51dba")
        ),
        browser(
            "com.yandex.browser.alpha",
            KnownApplication.sha256("aca405ded8b25cb2e8c6da69425d2b4307d087c1276fc06ad5942731ccc51dba")
        ),
        browser(
            "com.vivaldi.browser",
            KnownApplication.sha256("e8a78544655ba8c09817f732768f5689b1662ec4b2bc5a0bc0ec138d33ca3d1e")
        ),
        browser(
            "com.vivaldi.browser.snapshot",
            KnownApplication.sha256("e8a78544655ba8c09817f732768f5689b1662ec4b2bc5a0bc0ec138d33ca3d1e")
        ),
        browser(
            "com.qwant.liberty",
            KnownApplication.sha256("07168685759fd8ca0388b3c6bb6c18fcc100bcd5a9f3b326b77b2659ba79bb0f")
        ),
        browser(
            "org.torproject.torbrowser",
            KnownApplication.sha256("20061f045e737c67375c17794cfedb436a03cec6bacb7cb9f96642205ca2cec8")
        ),
        browser(
            "com.mi.globalbrowser",
            KnownApplication.sha256("c9009d01ebf9f5d0302bc71b2fe9aa9a47a432bba17308a3111b75d7b2149025")
        ),
        browser(
            "com.microsoft.bing",
            KnownApplication.sha256("ac46aba9236ebd5aed35994e9e88ee75d1d6b510e1d5f141b719da62dc3586fa")
        ) 
    )

    private fun browser(packageName: String, signature: KnownApplication.Signature): KnownApplication =
        Browser(packageName, signature.toAppSignature(packageName))

    fun getAppForPackage(packageName: String) = browsersList.singleOrNull { it.packageName == packageName }
}
