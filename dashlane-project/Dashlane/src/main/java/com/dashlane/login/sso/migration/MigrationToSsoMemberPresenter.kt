package com.dashlane.login.sso.migration

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.authentication.sso.GetUserSsoInfoActivity
import com.dashlane.authentication.sso.utils.UserSsoInfo
import com.dashlane.login.sso.LoginSsoActivity
import com.dashlane.login.sso.LoginSsoLogger
import com.dashlane.masterpassword.ChangeMasterPasswordLogoutHelper
import com.dashlane.util.coroutines.getDeferredViewModel
import com.dashlane.util.getParcelableCompat
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.util.inject.qualifiers.ActivityLifecycleCoroutineScope
import com.dashlane.util.inject.qualifiers.MainImmediateCoroutineDispatcher
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import javax.inject.Inject

class MigrationToSsoMemberPresenter @Inject constructor(
    @ActivityLifecycleCoroutineScope
    private val activityLifecycleCoroutineScope: CoroutineScope,
    @MainImmediateCoroutineDispatcher
    private val mainImmediateCoroutineDispatcher: CoroutineDispatcher,
    private val ssoLogger: LoginSsoLogger,
    private val logoutHelper: ChangeMasterPasswordLogoutHelper,
    activity: FragmentActivity
) : BasePresenter<MigrationToSsoMemberContract.DataProvider, MigrationToSsoMemberContract.ViewProxy>(),
    MigrationToSsoMemberContract.Presenter {
    private lateinit var login: String
    private var userSsoInfo: UserSsoInfo? = null

    private val deferredViewModel =
        ViewModelProvider(activity).getDeferredViewModel<Intent>("migrate_to_sso_member")

    override fun onCreate(savedInstanceState: Bundle?, login: String, serviceProviderUrl: String, isNitroProvider: Boolean) {
        this.login = login

        if (savedInstanceState == null) {
            ssoLogger.logLoginStart()
            activity?.run {
                startActivityForResult(
                    GetUserSsoInfoActivity.createStartIntent(this, login, serviceProviderUrl, isNitroProvider),
                    REQUEST_CODE_GET_USER_SSO_INFO
                )
            }
            return
        }

        userSsoInfo = savedInstanceState.getParcelableCompat(STATE_USER_SSO_INFO)

        val deferred = deferredViewModel.deferred

        when {
            deferred != null -> await(deferred)
            userSsoInfo != null -> migrateToSsoMember()
            else -> logout()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CODE_GET_USER_SSO_INFO) return

        if (resultCode == Activity.RESULT_CANCELED) {
            logout()
            return
        }

        val result = data?.getParcelableExtraCompat<GetSsoInfoResult>(LoginSsoActivity.KEY_RESULT)

        if (result !is GetSsoInfoResult.Success) {
            logout()
            return
        }

        userSsoInfo = result.userSsoInfo
        migrateToSsoMember()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(STATE_USER_SSO_INFO, userSsoInfo)
    }

    private fun migrateToSsoMember() {
        val deferred = deferredViewModel.async {
            provider.migrateToSsoMember(login, userSsoInfo!!)
        }
        await(deferred)
    }

    private fun await(deferred: Deferred<Intent>) {
        activityLifecycleCoroutineScope.launch(mainImmediateCoroutineDispatcher) {
            try {
                val intent = deferred.await()
                notifySuccess(intent)
            } catch (e: CancellationException) {
                
            } catch (e: Exception) {
                notifyError()
            }
        }
    }

    private fun notifySuccess(intent: Intent) {
        activity?.run {
            startActivity(intent)
            finish()
        }
    }

    private fun notifyError() {
        logout()
    }

    private fun logout() {
        activity?.let { logoutHelper.logout(it) }
    }

    companion object {
        private const val REQUEST_CODE_GET_USER_SSO_INFO = 24_763

        private const val STATE_USER_SSO_INFO = "user_sso_info"
    }
}
