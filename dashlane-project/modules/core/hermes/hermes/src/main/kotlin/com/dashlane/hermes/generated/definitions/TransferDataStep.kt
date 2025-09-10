package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class TransferDataStep(override val code: String) : TrackingLog.Enum {
    CREDENTIAL_EXCHANGE_PROTOCOL(
        code = "credential_exchange_protocol",
    ),
    DECRYPT_LASTPASS_VAULT(
        code = "decrypt_lastpass_vault",
    ),
    IMPORTING_FILE(
        code = "importing_file",
    ),
    LOGIN_TO_LASTPASS(
        code = "login_to_lastpass",
    ),
    PREVIEW_ITEMS_TO_EXPORT(
        code = "preview_items_to_export",
    ),
    PREVIEW_ITEMS_TO_IMPORT(
        code = "preview_items_to_import",
    ),
    SELECT_DASHLANE_SPACE(
        code = "select_dashlane_space",
    ),
    SELECT_EXPORT_DESTINATION(
        code = "select_export_destination",
    ),
    SELECT_FILE(
        code = "select_file",
    ),
    SELECT_IMPORT_SOURCE(
        code = "select_import_source",
    ),
    SUCCESS(
        code = "success",
    ),
}