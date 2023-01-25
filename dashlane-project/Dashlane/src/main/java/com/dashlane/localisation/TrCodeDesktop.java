package com.dashlane.localisation;

import com.dashlane.R;

public enum TrCodeDesktop {

    UI_SHARING_ERROR_DEFAULT(R.string.ui_sharing_error_default),
    UI_SHARING_ERROR_LOCK_ALREADY_ACQUIRED(R.string.ui_sharing_error_lock_already_acquired),
    UI_SHARING_ERROR_TIMER_PREVENT_SHARING(R.string.ui_sharing_error_timer_prevent_sharing),
    UI_SHARING_ERROR_NOTHING_TO_SHARE(R.string.ui_sharing_error_nothing_to_share),
    UI_SHARING_ERROR_FAIL_APPLYING_LOCAL_CHANGES(R.string.ui_sharing_error_fail_applying_local_changes),
    UI_SHARING_ERROR_PUBLIC_KEYS_NOT_FOUND(R.string.ui_sharing_error_public_keys_not_found),
    UI_SHARING_ERROR_NON_TRANSACTIONNAL_EVENTS_1(R.string.ui_sharing_error_non_transactionnal_events_1),
    UI_SHARING_ERROR_NON_TRANSACTIONNAL_EVENTS_2(R.string.ui_sharing_error_non_transactionnal_events_2),
    UI_SHARING_ERROR_WRONG_LOCK(R.string.ui_sharing_error_wrong_lock),
    UI_SHARING_ERROR_NETWORK_OR_SERVER_ERROR(R.string.ui_sharing_error_network_or_server_error),
    UI_SHARING_ERROR_UNKNOWN(R.string.ui_sharing_error_unknown),
    UI_SHARING_ERROR_UNKNOWN_2(R.string.ui_sharing_error_unknown_2),
    UI_SHARING_ERROR_UNKNOWN_3(R.string.ui_sharing_error_unknown_3),
    UI_SHARING_ERROR_CANNOT_SHARE_WITH_ONLY_YOU(R.string.ui_sharing_error_cannot_share_with_only_you),
    UI_SHARING_ERROR_SHARING_TWICE_AN_ITEM(R.string.ui_sharing_error_sharing_twice_an_item),
    UI_SHARING_ERROR_CANNOT_CREATE_SHARING_KEYS(R.string.ui_sharing_error_cannot_create_sharing_keys),
    UI_SHARING_ERROR_CONFLICT_REMOTE_MODIFICATION_ON_THIS_GROUP(R.string
                                                                        .ui_sharing_error_conflict_remote_modification_on_this_group),
    UI_SHARING_ERROR_FAIL_APPLYING_LOCAL_CHANGES_2(R.string.ui_sharing_error_fail_applying_local_changes_2),
    UI_SHARING_ERROR_FAIL_APPLYING_LOCAL_CHANGES_3(R.string.ui_sharing_error_fail_applying_local_changes_3),
    UI_SHARING_ERROR_ADMIN_RIGHTS_ISSUE(R.string.ui_sharing_error_admin_rights_issue),
    UI_SHARING_ERROR_USER_NOT_ACTIVE(R.string.ui_sharing_error_user_not_active),
    UI_SHARING_ERROR_ACTION_NOT_VALID(R.string.ui_sharing_error_action_not_valid),
    UI_SHARING_ERROR_PUBLIC_KEY_NOT_READY(R.string.ui_sharing_error_public_key_not_ready),
    UI_SHARING_ERROR_DATA_NOT_READY(R.string.ui_sharing_error_data_not_ready),
    UI_SHARING_ERROR_PLEASE_UPDATE_WRONG_SHARING_VERSION(R.string.ui_sharing_error_please_update_wrong_sharing_version),
    UI_SHARING_ERROR_SHARING_SKIPPED(R.string.ui_sharing_error_sharing_skipped);

    private int mStringRes;

    TrCodeDesktop(int stringRes) {
        mStringRes = stringRes;
    }

    public int getStringResource() {
        return mStringRes;
    }
}
