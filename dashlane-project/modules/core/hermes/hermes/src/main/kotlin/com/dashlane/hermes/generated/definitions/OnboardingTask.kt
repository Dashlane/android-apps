package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class OnboardingTask(override val code: String) : TrackingLog.Enum {
    ADD_FIRST_LOGIN(
        code = "add_first_login",
    ),
    ASSIGN_NEW_ADMIN(
        code = "assign_new_admin",
    ),
    CLOSE_GUIDE(
        code = "close_guide",
    ),
    CREATE_SHARING_GROUP(
        code = "create_sharing_group",
    ),
    GET_MOBILE_APP(
        code = "get_mobile_app",
    ),
    INVITE_PLAN_MEMBERS(
        code = "invite_plan_members",
    ),
    SHARE_ITEM(
        code = "share_item",
    ),
    TRY_AUTOFILL(
        code = "try_autofill",
    ),
    VISIT_DASHBOARD_PASSWORD_HEALTH(
        code = "visit_dashboard_password_health",
    ),
    VISIT_VAULT(
        code = "visit_vault",
    ),
}