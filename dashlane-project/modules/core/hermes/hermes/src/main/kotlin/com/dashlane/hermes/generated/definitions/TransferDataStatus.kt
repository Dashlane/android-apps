package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class TransferDataStatus(override val code: String) : TrackingLog.Enum {
    EXPORT_FLOW_STARTED(
        code = "export_flow_started",
    ),
    FAILED_TO_DECRYPT_VAULT(
        code = "failed_to_decrypt_vault",
    ),
    FAILED_TO_FETCH_VAULT(
        code = "failed_to_fetch_vault",
    ),
    FAILURE_DURING_EXPORT(
        code = "failure_during_export",
    ),
    FAILURE_DURING_IMPORT(
        code = "failure_during_import",
    ),
    FILE_MATCHED(
        code = "file_matched",
    ),
    IMPORT_FLOW_STARTED(
        code = "import_flow_started",
    ),
    IMPORT_FLOW_TERMINATED(
        code = "import_flow_terminated",
    ),
    IMPORT_FLOW_TIMEOUT(
        code = "import_flow_timeout",
    ),
    MULTIPLE_FILES_SELECTED(
        code = "multiple_files_selected",
    ),
    NO_DATA_FOUND(
        code = "no_data_found",
    ),
    PENDING_SOURCE_SELECTION(
        code = "pending_source_selection",
    ),
    PROCESS_FAILURE(
        code = "process_failure",
    ),
    PROCESS_TERMINATED(
        code = "process_terminated",
    ),
    PROCESS_TIMEOUT(
        code = "process_timeout",
    ),
    START(
        code = "start",
    ),
    SUCCESS(
        code = "success",
    ),
    WRONG_FILE_FORMAT(
        code = "wrong_file_format",
    ),
    WRONG_FILE_PASSWORD(
        code = "wrong_file_password",
    ),
    WRONG_FILE_STRUCTURE(
        code = "wrong_file_structure",
    ),
}