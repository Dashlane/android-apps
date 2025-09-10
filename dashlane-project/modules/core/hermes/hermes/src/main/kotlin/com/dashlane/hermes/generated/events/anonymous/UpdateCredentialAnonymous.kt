package com.dashlane.hermes.generated.events.anonymous

import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.TrackingLog
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.CredentialSecurityStatus
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.Space
import com.dashlane.hermes.generated.definitions.UpdateCredentialOrigin
import javax.annotation.processing.Generated

@Generated
data class UpdateCredentialAnonymous(
    val isCredentialCurrentlyEligibleToPasswordChanger: Boolean? = null,
    val associatedWebsitesRemovedList: List<Sha256Hash>? = null,
    val associatedAppsAddedList: List<Sha256Hash>? = null,
    val credentialSecurityStatus: CredentialSecurityStatus? = null,
    val space: Space,
    val credentialOriginalSecurityStatus: CredentialSecurityStatus? = null,
    val updateCredentialOrigin: UpdateCredentialOrigin? = null,
    val domain: Domain,
    val action: Action,
    val associatedWebsitesAddedList: List<Sha256Hash>? = null,
    val fieldList: List<Field>? = null,
    val associatedAppsRemovedList: List<Sha256Hash>? = null,
) : TrackingLog {
    override val name
        get() = "update_credential"

    override val category
        get() = TrackingLog.Category.ANONYMOUS

    override val priority
        get() = false

    override fun collect(collector: TrackingLog.Collector) {
        collector["name"] = name
        collector["is_credential_currently_eligible_to_password_changer"] = isCredentialCurrentlyEligibleToPasswordChanger
        collector["associated_websites_removed_list"] = associatedWebsitesRemovedList
        collector["associated_apps_added_list"] = associatedAppsAddedList
        collector["credential_security_status"] = credentialSecurityStatus
        collector["space"] = space
        collector["credential_original_security_status"] = credentialOriginalSecurityStatus
        collector["update_credential_origin"] = updateCredentialOrigin
        collector["domain"] = domain
        collector["action"] = action
        collector["associated_websites_added_list"] = associatedWebsitesAddedList
        collector["field_list"] = fieldList
        collector["associated_apps_removed_list"] = associatedAppsRemovedList
    }
}