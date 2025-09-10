package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class TransferDataSource(override val code: String) : TrackingLog.Enum {
    SOURCE_1PASSWORD(
        code = "source_1password",
    ),
    SOURCE_APPLE_PASSWORDS(
        code = "source_apple_passwords",
    ),
    SOURCE_BITWARDEN(
        code = "source_bitwarden",
    ),
    SOURCE_CHROME(
        code = "source_chrome",
    ),
    SOURCE_DASH(
        code = "source_dash",
    ),
    SOURCE_EDGE(
        code = "source_edge",
    ),
    SOURCE_FIREFOX(
        code = "source_firefox",
    ),
    SOURCE_KEEPASS(
        code = "source_keepass",
    ),
    SOURCE_KEEPER(
        code = "source_keeper",
    ),
    SOURCE_KEYCHAIN(
        code = "source_keychain",
    ),
    SOURCE_LASTPASS(
        code = "source_lastpass",
    ),
    SOURCE_OTHER(
        code = "source_other",
    ),
    SOURCE_SAFARI(
        code = "source_safari",
    ),
    SOURCE_URLS_FOR_ALLOWLIST(
        code = "source_urls_for_allowlist",
    ),
}