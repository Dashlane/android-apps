package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class WebauthnExtensionId(override val code: String) : TrackingLog.Enum {
    APPID(
        code = "appid",
    ),
    APPID_EXCLUDE(
        code = "appid_exclude",
    ),
    AUTHN_SEL(
        code = "authn_sel",
    ),
    CRED_BLOB(
        code = "cred_blob",
    ),
    CRED_PROPS(
        code = "cred_props",
    ),
    CRED_PROTECT(
        code = "cred_protect",
    ),
    EXTS(
        code = "exts",
    ),
    HMAC_SECRET(
        code = "hmac_secret",
    ),
    LARGE_BLOB(
        code = "large_blob",
    ),
    LARGE_BLOB_KEY(
        code = "large_blob_key",
    ),
    MIN_PIN_LENGTH(
        code = "min_pin_length",
    ),
    TX_AUTH_GENERIC(
        code = "tx_auth_generic",
    ),
    TX_AUTH_SIMPLE(
        code = "tx_auth_simple",
    ),
    UVI(
        code = "uvi",
    ),
    UVM(
        code = "uvm",
    ),
}