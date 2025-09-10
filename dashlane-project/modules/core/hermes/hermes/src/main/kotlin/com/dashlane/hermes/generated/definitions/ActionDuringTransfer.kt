package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ActionDuringTransfer(override val code: String) : TrackingLog.Enum {
    ACCESS_VAULT(
        code = "access_vault",
    ),
    CANCEL_DEVICE_TRANSFER(
        code = "cancel_device_transfer",
    ),
    CANCEL_LOGIN(
        code = "cancel_login",
    ),
    COMPLETE_DEVICE_TRANSFER(
        code = "complete_device_transfer",
    ),
    CONFIRM_REQUEST(
        code = "confirm_request",
    ),
    CONTACT_SUPPORT(
        code = "contact_support",
    ),
    REFRESH_REQUEST(
        code = "refresh_request",
    ),
    REJECT_REQUEST(
        code = "reject_request",
    ),
    RETURN_TO_DEVICE_SETUP(
        code = "return_to_device_setup",
    ),
    RETURN_TO_LOGIN(
        code = "return_to_login",
    ),
    SCAN_QR_CODE(
        code = "scan_qr_code",
    ),
    SELECT_LOGGED_IN_DEVICE(
        code = "select_logged_in_device",
    ),
    SELECT_TRANSFER_METHOD(
        code = "select_transfer_method",
    ),
    SET_PIN(
        code = "set_pin",
    ),
    SETUP_BIOMETRICS(
        code = "setup_biometrics",
    ),
    SUBMIT_SECURITY_CHALLENGE_ANSWER(
        code = "submit_security_challenge_answer",
    ),
    TAP_HELP(
        code = "tap_help",
    ),
    TAP_LEARN_MORE(
        code = "tap_learn_more",
    ),
    TAP_RESET_ACCOUNT(
        code = "tap_reset_account",
    ),
    TRY_AGAIN(
        code = "try_again",
    ),
    VIEW_SECURITY_CHALLENGE_SOLUTION(
        code = "view_security_challenge_solution",
    ),
}