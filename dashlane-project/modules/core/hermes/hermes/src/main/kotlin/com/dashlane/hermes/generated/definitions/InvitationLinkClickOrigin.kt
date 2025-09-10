package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class InvitationLinkClickOrigin(override val code: String) : TrackingLog.Enum {
    EXTENSION_MASS_DEPLOYMENT(
        code = "extension_mass_deployment",
    ),
    INVITATION_EMAIL(
        code = "invitation_email",
    ),
    SHARED_INVITATION_LINK(
        code = "shared_invitation_link",
    ),
}