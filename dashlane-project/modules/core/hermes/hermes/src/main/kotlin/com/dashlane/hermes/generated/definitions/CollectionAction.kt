package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class CollectionAction(override val code: String) : TrackingLog.Enum {
    ADD(
        code = "add",
    ),
    ADD_CREDENTIAL(
        code = "add_credential",
    ),
    DELETE(
        code = "delete",
    ),
    DELETE_CREDENTIAL(
        code = "delete_credential",
    ),
    EDIT(
        code = "edit",
    ),
    RENAME(
        code = "rename",
    ),
    RENAME_CREDENTIAL(
        code = "rename_credential",
    ),
}