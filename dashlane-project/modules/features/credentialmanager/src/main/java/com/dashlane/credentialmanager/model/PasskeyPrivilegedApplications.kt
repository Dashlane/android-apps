package com.dashlane.credentialmanager.model

import com.dashlane.credentialmanager.model.PrivilegedAllowlist.PrivilegedAllowlistApp
import com.dashlane.credentialmanager.model.PrivilegedAllowlist.PrivilegedAllowlistSignature
import com.dashlane.credentialmanager.model.PrivilegedAllowlist.PrivilegedAllowlistType

@Suppress(
    "kotlin:S1192", 
    "LargeClass"
)
object PasskeyPrivilegedApplications {
    val allowList: PrivilegedAllowlist
        get() = PrivilegedAllowlist(
            apps = application.map {
                PrivilegedAllowlistType(
                    type = "android",
                    info = it
                )
            }
        )

    private val application: List<PrivilegedAllowlistApp>
        get() = listOf(
            
            app(
                packageName = "com.android.chrome",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "F0:FD:6C:5B:41:0F:25:CB:25:C3:B5:33:46:C8:97:2F:AE:30:F8:EE:74:11:DF:91:04:80:AD:6B:2D:60:DB:83"
                    ),
                    signature(
                        build = "userdebug",
                        sha256 = "19:75:B2:F1:71:77:BC:89:A5:DF:F3:1F:9E:64:A6:CA:E2:81:A5:3D:C1:D1:D5:9B:1D:14:7F:E1:C8:2A:FA:00"
                    ),
                )
            ),
            app(
                packageName = "com.chrome.beta",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "DA:63:3D:34:B6:9E:63:AE:21:03:B4:9D:53:CE:05:2F:C5:F7:F3:C5:3A:AB:94:FD:C2:A2:08:BD:FD:14:24:9C"
                    ),
                    signature(
                        build = "release",
                        sha256 = "3D:7A:12:23:01:9A:A3:9D:9E:A0:E3:43:6A:B7:C0:89:6B:FB:4F:B6:79:F4:DE:5F:E7:C2:3F:32:6C:8F:99:4A"
                    ),
                )
            ),
            app(
                packageName = "com.chrome.dev",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "90:44:EE:5F:EE:4B:BC:5E:21:DD:44:66:54:31:C4:EB:1F:1F:71:A3:27:16:A0:BC:92:7B:CB:B3:92:33:CA:BF"
                    ),
                    signature(
                        build = "release",
                        sha256 = "3D:7A:12:23:01:9A:A3:9D:9E:A0:E3:43:6A:B7:C0:89:6B:FB:4F:B6:79:F4:DE:5F:E7:C2:3F:32:6C:8F:99:4A"
                    ),
                )
            ),
            app(
                packageName = "com.chrome.canary",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "20:19:DF:A1:FB:23:EF:BF:70:C5:BC:D1:44:3C:5B:EA:B0:4F:3F:2F:F4:36:6E:9A:C1:E3:45:76:39:A2:4C:FC"
                    ),
                )
            ),
            app(
                packageName = "org.chromium.chrome",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "C6:AD:B8:B8:3C:6D:4C:17:D2:92:AF:DE:56:FD:48:8A:51:D3:16:FF:8F:2C:11:C5:41:02:23:BF:F8:A7:DB:B3"
                    ),
                    signature(
                        build = "userdebug",
                        sha256 = "19:75:B2:F1:71:77:BC:89:A5:DF:F3:1F:9E:64:A6:CA:E2:81:A5:3D:C1:D1:D5:9B:1D:14:7F:E1:C8:2A:FA:00"
                    ),
                )
            ),
            app(
                packageName = "com.google.android.apps.chrome",
                signatures = listOf(
                    signature(
                        build = "userdebug",
                        sha256 = "19:75:B2:F1:71:77:BC:89:A5:DF:F3:1F:9E:64:A6:CA:E2:81:A5:3D:C1:D1:D5:9B:1D:14:7F:E1:C8:2A:FA:00"
                    ),
                )
            ),

            
            app(
                packageName = "org.mozilla.fennec_webauthndebug",
                signatures = listOf(
                    signature(
                        build = "userdebug",
                        sha256 = "BD:AE:82:02:80:D2:AF:B7:74:94:EF:22:58:AA:78:A9:AE:A1:36:41:7E:8B:C2:3D:C9:87:75:2E:6F:48:E8:48"
                    ),
                )
            ),
            app(
                packageName = "org.mozilla.firefox",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "A7:8B:62:A5:16:5B:44:94:B2:FE:AD:9E:76:A2:80:D2:2D:93:7F:EE:62:51:AE:CE:59:94:46:B2:EA:31:9B:04"
                    ),
                )
            ),
            app(
                packageName = "org.mozilla.firefox_beta",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "A7:8B:62:A5:16:5B:44:94:B2:FE:AD:9E:76:A2:80:D2:2D:93:7F:EE:62:51:AE:CE:59:94:46:B2:EA:31:9B:04"
                    ),
                )
            ),
            app(
                packageName = "org.mozilla.focus",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "62:03:A4:73:BE:36:D6:4E:E3:7F:87:FA:50:0E:DB:C7:9E:AB:93:06:10:AB:9B:9F:A4:CA:7D:5C:1F:1B:4F:FC"
                    ),
                )
            ),
            app(
                packageName = "org.mozilla.fennec_aurora",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "BC:04:88:83:8D:06:F4:CA:6B:F3:23:86:DA:AB:0D:D8:EB:CF:3E:77:30:78:74:59:F6:2F:B3:CD:14:A1:BA:AA"
                    ),
                )
            ),
            app(
                packageName = "org.mozilla.rocket",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "86:3A:46:F0:97:39:32:B7:D0:19:9B:54:91:12:74:1C:2D:27:31:AC:72:EA:11:B7:52:3A:A9:0A:11:BF:56:91"
                    ),
                )
            ),

            
            app(
                packageName = "com.microsoft.emmx.canary",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "01:E1:99:97:10:A8:2C:27:49:B4:D5:0C:44:5D:C8:5D:67:0B:61:36:08:9D:0A:76:6A:73:82:7C:82:A1:EA:C9"
                    ),
                )
            ),
            app(
                packageName = "com.microsoft.emmx.dev",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "01:E1:99:97:10:A8:2C:27:49:B4:D5:0C:44:5D:C8:5D:67:0B:61:36:08:9D:0A:76:6A:73:82:7C:82:A1:EA:C9"
                    ),
                )
            ),
            app(
                packageName = "com.microsoft.emmx.beta",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "01:E1:99:97:10:A8:2C:27:49:B4:D5:0C:44:5D:C8:5D:67:0B:61:36:08:9D:0A:76:6A:73:82:7C:82:A1:EA:C9"
                    ),
                )
            ),
            app(
                packageName = "com.microsoft.emmx",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "01:E1:99:97:10:A8:2C:27:49:B4:D5:0C:44:5D:C8:5D:67:0B:61:36:08:9D:0A:76:6A:73:82:7C:82:A1:EA:C9"
                    ),
                )
            ),
            app(
                packageName = "com.microsoft.emmx.rolling",
                signatures = listOf(
                    signature(
                        build = "userdebug",
                        sha256 = "32:A2:FC:74:D7:31:10:58:59:E5:A8:5D:F1:6D:95:F1:02:D8:5B:22:09:9B:80:64:C5:D8:91:5C:61:DA:D1:E0"
                    ),
                )
            ),
            app(
                packageName = "com.microsoft.emmx.local",
                signatures = listOf(
                    signature(
                        build = "userdebug",
                        sha256 = "32:A2:FC:74:D7:31:10:58:59:E5:A8:5D:F1:6D:95:F1:02:D8:5B:22:09:9B:80:64:C5:D8:91:5C:61:DA:D1:E0"
                    ),
                )
            ),

            
            app(
                packageName = "com.brave.browser",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "9C:2D:B7:05:13:51:5F:DB:FB:BC:58:5B:3E:DF:3D:71:23:D4:DC:67:C9:4F:FD:30:63:61:C1:D7:9B:BF:18:AC"
                    ),
                )
            ),
            app(
                packageName = "com.brave.browser_beta",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "9C:2D:B7:05:13:51:5F:DB:FB:BC:58:5B:3E:DF:3D:71:23:D4:DC:67:C9:4F:FD:30:63:61:C1:D7:9B:BF:18:AC"
                    ),
                )
            ),
            app(
                packageName = "com.brave.browser_nightly",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "9C:2D:B7:05:13:51:5F:DB:FB:BC:58:5B:3E:DF:3D:71:23:D4:DC:67:C9:4F:FD:30:63:61:C1:D7:9B:BF:18:AC"
                    ),
                )
            ),
            app(
                packageName = "app.vanadium.browser",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "C6:AD:B8:B8:3C:6D:4C:17:D2:92:AF:DE:56:FD:48:8A:51:D3:16:FF:8F:2C:11:C5:41:02:23:BF:F8:A7:DB:B3"
                    ),
                )
            ),

            
            app(
                packageName = "com.vivaldi.browser",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "E8:A7:85:44:65:5B:A8:C0:98:17:F7:32:76:8F:56:89:B1:66:2E:C4:B2:BC:5A:0B:C0:EC:13:8D:33:CA:3D:1E"
                    ),
                )
            ),
            app(
                packageName = "com.vivaldi.browser.snapshot",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "E8:A7:85:44:65:5B:A8:C0:98:17:F7:32:76:8F:56:89:B1:66:2E:C4:B2:BC:5A:0B:C0:EC:13:8D:33:CA:3D:1E"
                    ),
                )
            ),
            app(
                packageName = "com.vivaldi.browser.sopranos",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "E8:A7:85:44:65:5B:A8:C0:98:17:F7:32:76:8F:56:89:B1:66:2E:C4:B2:BC:5A:0B:C0:EC:13:8D:33:CA:3D:1E"
                    ),
                )
            ),
            app(
                packageName = "com.citrix.Receiver",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "3D:D1:12:67:10:69:AB:36:4E:F9:BE:73:9A:B7:B5:EE:15:E1:CD:E9:D8:75:7B:1B:F0:64:F5:0C:55:68:9A:49"
                    ),
                    signature(
                        build = "release",
                        sha256 = "CE:B2:23:D7:77:09:F2:B6:BC:0B:3A:78:36:F5:A5:AF:4C:E1:D3:55:F4:A7:28:86:F7:9D:F8:0D:C9:D6:12:2E"
                    ),
                    signature(
                        build = "release",
                        sha256 = "AA:D0:D4:57:E6:33:C3:78:25:77:30:5B:C1:B2:D9:E3:81:41:C7:21:DF:0D:AA:6E:29:07:2F:C4:1D:34:F0:AB"
                    ),
                )
            ),

            
            app(
                packageName = "com.android.browser",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "C9:00:9D:01:EB:F9:F5:D0:30:2B:C7:1B:2F:E9:AA:9A:47:A4:32:BB:A1:73:08:A3:11:1B:75:D7:B2:14:90:25"
                    ),
                )
            ),
            app(
                packageName = "com.sec.android.app.sbrowser",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "C8:A2:E9:BC:CF:59:7C:2F:B6:DC:66:BE:E2:93:FC:13:F2:FC:47:EC:77:BC:6B:2B:0D:52:C1:1F:51:19:2A:B8"
                    ),
                    signature(
                        build = "release",
                        sha256 = "34:DF:0E:7A:9F:1C:F1:89:2E:45:C0:56:B4:97:3C:D8:1C:CF:14:8A:40:50:D1:1A:EA:4A:C5:A6:5F:90:0A:42"
                    ),
                )
            ),
            app(
                packageName = "com.sec.android.app.sbrowser.beta",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "C8:A2:E9:BC:CF:59:7C:2F:B6:DC:66:BE:E2:93:FC:13:F2:FC:47:EC:77:BC:6B:2B:0D:52:C1:1F:51:19:2A:B8"
                    ),
                    signature(
                        build = "release",
                        sha256 = "34:DF:0E:7A:9F:1C:F1:89:2E:45:C0:56:B4:97:3C:D8:1C:CF:14:8A:40:50:D1:1A:EA:4A:C5:A6:5F:90:0A:42"
                    ),
                )
            ),
            app(
                packageName = "com.google.android.gms",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "7C:E8:3C:1B:71:F3:D5:72:FE:D0:4C:8D:40:C5:CB:10:FF:75:E6:D8:7D:9D:F6:FB:D5:3F:04:68:C2:90:50:53"
                    ),
                    signature(
                        build = "release",
                        sha256 = "D2:2C:C5:00:29:9F:B2:28:73:A0:1A:01:0D:E1:C8:2F:BE:4D:06:11:19:B9:48:14:DD:30:1D:AB:50:CB:76:78"
                    ),
                    signature(
                        build = "release",
                        sha256 = "F0:FD:6C:5B:41:0F:25:CB:25:C3:B5:33:46:C8:97:2F:AE:30:F8:EE:74:11:DF:91:04:80:AD:6B:2D:60:DB:83"
                    ),
                    signature(
                        build = "release",
                        sha256 = "19:75:B2:F1:71:77:BC:89:A5:DF:F3:1F:9E:64:A6:CA:E2:81:A5:3D:C1:D1:D5:9B:1D:14:7F:E1:C8:2A:FA:00"
                    ),
                )
            ),

            
            app(
                packageName = "com.yandex.browser",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "AC:A4:05:DE:D8:B2:5C:B2:E8:C6:DA:69:42:5D:2B:43:07:D0:87:C1:27:6F:C0:6A:D5:94:27:31:CC:C5:1D:BA"
                    ),
                )
            ),
            app(
                packageName = "com.yandex.browser.beta",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "AC:A4:05:DE:D8:B2:5C:B2:E8:C6:DA:69:42:5D:2B:43:07:D0:87:C1:27:6F:C0:6A:D5:94:27:31:CC:C5:1D:BA"
                    ),
                )
            ),
            app(
                packageName = "com.yandex.browser.alpha",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "AC:A4:05:DE:D8:B2:5C:B2:E8:C6:DA:69:42:5D:2B:43:07:D0:87:C1:27:6F:C0:6A:D5:94:27:31:CC:C5:1D:BA"
                    ),
                )
            ),
            app(
                packageName = "com.yandex.browser.corp",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "AC:A4:05:DE:D8:B2:5C:B2:E8:C6:DA:69:42:5D:2B:43:07:D0:87:C1:27:6F:C0:6A:D5:94:27:31:CC:C5:1D:BA"
                    ),
                )
            ),
            app(
                packageName = "com.yandex.browser.canary",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "1D:A9:CB:AE:2D:CC:C6:A5:8D:6C:94:7B:E9:4C:DB:B7:33:D6:5D:A4:D1:77:0F:A1:4A:53:64:CB:4A:28:EB:49"
                    ),
                )
            ),
            app(
                packageName = "com.yandex.browser.broteam",
                signatures = listOf(
                    signature(
                        build = "release",
                        sha256 = "1D:A9:CB:AE:2D:CC:C6:A5:8D:6C:94:7B:E9:4C:DB:B7:33:D6:5D:A4:D1:77:0F:A1:4A:53:64:CB:4A:28:EB:49"
                    ),
                )
            ),
        )

    private fun app(packageName: String, signatures: List<PrivilegedAllowlistSignature>): PrivilegedAllowlistApp =
        PrivilegedAllowlistApp(
            packageName = packageName,
            signatures = signatures
        )

    private fun signature(build: String, sha256: String): PrivilegedAllowlistSignature =
        PrivilegedAllowlistSignature(
            build = build,
            sha256 = sha256
        )
}
