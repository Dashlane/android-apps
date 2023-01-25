package com.dashlane.login.sso.migration

import android.content.Intent
import android.os.Bundle
import com.dashlane.authentication.sso.utils.UserSsoInfo
import com.skocken.presentation.definition.Base

interface MigrationToSsoMemberContract {
    interface ViewProxy : Base.IView

    interface Presenter : Base.IPresenter {
        fun onCreate(savedInstanceState: Bundle?, login: String, serviceProviderUrl: String, isNitroProvider: Boolean)
        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
        fun onSaveInstanceState(outState: Bundle)
    }

    interface DataProvider : Base.IDataProvider {
        suspend fun migrateToSsoMember(login: String, userSsoInfo: UserSsoInfo): Intent
    }
}