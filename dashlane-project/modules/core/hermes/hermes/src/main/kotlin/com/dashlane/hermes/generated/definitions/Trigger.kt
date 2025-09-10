package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class Trigger(override val code: String) : TrackingLog.Enum {
    ACCOUNT_CREATION(
        code = "account_creation",
    ),
    CHANGE_MASTER_PASSWORD(
        code = "change_master_password",
    ),
    CHANGE_TEAM(
        code = "change_team",
    ),
    INITIAL_LOGIN(
        code = "initial_login",
    ),
    LOGIN(
        code = "login",
    ),
    MANUAL(
        code = "manual",
    ),
    PERIODIC(
        code = "periodic",
    ),
    PUSH(
        code = "push",
    ),
    SAVE(
        code = "save",
    ),
    SAVE_META(
        code = "save_meta",
    ),
    SETTINGS_CHANGE(
        code = "settings_change",
    ),
    SHARING(
        code = "sharing",
    ),
    WAKE(
        code = "wake",
    ),
}