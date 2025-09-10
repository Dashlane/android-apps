package com.dashlane.hermes.generated.events.user

import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.CredentialSecurityStatus
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.Space
import com.dashlane.hermes.generated.definitions.UpdateCredentialOrigin
import java.util.UUID
import javax.annotation.processing.Generated

@Generated
data class UpdateVaultItem(
    val credentialOriginalSecurityStatus: CredentialSecurityStatus? = null,
    val isCredentialCurrentlyEligibleToPasswordChanger: Boolean? = null,
    val updateCredentialOrigin: UpdateCredentialOrigin? = null,
    val itemId: ItemId,
    val itemType: ItemType,
    val action: Action,
    val collectionCount: Int? = null,
    val fieldsEdited: List<Field>? = null,
    val credentialSecurityStatus: CredentialSecurityStatus? = null,
    val multiSelectId: UUID? = null,
    val space: Space,
) : TrackingLog {
    override val name
        get() = "update_vault_item"

    override val category
        get() = TrackingLog.Category.USER

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["credential_original_security_status"] = credentialOriginalSecurityStatus
        collector["is_credential_currently_eligible_to_password_changer"] = isCredentialCurrentlyEligibleToPasswordChanger
        collector["update_credential_origin"] = updateCredentialOrigin
        collector["item_id"] = itemId
        collector["item_type"] = itemType
        collector["action"] = action
        collector["collection_count"] = collectionCount
        collector["fields_edited"] = fieldsEdited
        collector["credential_security_status"] = credentialSecurityStatus
        collector["multi_select_id"] = multiSelectId
        collector["space"] = space
    }
}