package com.dashlane.login.sso.migration

import android.app.Activity
import com.dashlane.R
import com.dashlane.login.progress.LoginSyncProgressProcessPercentViewProxy
import com.skocken.presentation.viewproxy.BaseViewProxy

class MigrationToSsoMemberViewProxy(
    activity: Activity
) : BaseViewProxy<MigrationToSsoMemberContract.Presenter>(activity), MigrationToSsoMemberContract.ViewProxy {

    init {
        LoginSyncProgressProcessPercentViewProxy(activity.findViewById(R.id.progress_process_percent_layout)).apply {
            setMessage(activity.getString(R.string.sso_member_migration_message))
            setNotes("")
            showLoader()
        }
    }
}