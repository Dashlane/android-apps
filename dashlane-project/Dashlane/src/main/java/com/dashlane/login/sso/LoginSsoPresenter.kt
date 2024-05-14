package com.dashlane.login.sso

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.AuthenticationInvalidSsoException
import com.dashlane.authentication.AuthenticationNetworkException
import com.dashlane.authentication.AuthenticationOfflineException
import com.dashlane.authentication.AuthenticationUnknownException
import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.authentication.sso.GetUserSsoInfoActivity
import com.dashlane.authentication.sso.utils.UserSsoInfo
import com.dashlane.authentication.create.AccountCreator
import com.dashlane.util.coroutines.getDeferredViewModel
import com.dashlane.util.getParcelableCompat
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.util.inject.qualifiers.ActivityLifecycleCoroutineScope
import com.dashlane.util.inject.qualifiers.MainImmediateCoroutineDispatcher
import com.skocken.presentation.presenter.BasePresenter
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch

class LoginSsoPresenter @Inject constructor(
    @ActivityLifecycleCoroutineScope
    private val activityLifecycleCoroutineScope: CoroutineScope,
    @MainImmediateCoroutineDispatcher
    private val mainImmediateCoroutineDispatcher: CoroutineDispatcher,
    private val logger: LoginSsoLogger,
    activity: FragmentActivity,
) : BasePresenter<LoginSsoContract.DataProvider, LoginSsoContract.ViewProxy>(),
    LoginSsoContract.Presenter {
    private lateinit var login: String
    private var userSsoInfo: UserSsoInfo? = null

    private val deferredViewModel =
        ViewModelProvider(activity).getDeferredViewModel<Intent>("SsoLogin")

    override fun onCreate(
        savedInstanceState: Bundle?,
        login: String,
        serviceProviderUrl: String,
        isNitroProvider: Boolean
    ) {
        this.login = login
        if (savedInstanceState == null) {
            logger.logLoginStart()
            activity?.run {
                startActivityForResult(
                    GetUserSsoInfoActivity.createStartIntent(this, login, serviceProviderUrl, isNitroProvider),
                    REQUEST_CODE_GET_USER_SSO_INFO
                )
            }
        } else {
            val deferred = deferredViewModel.deferred
            val userSsoInfo = savedInstanceState.getParcelableCompat<UserSsoInfo>(STATE_USER_SSO_INFO)
                ?.also { userSsoInfo = it }

            when {
                deferred != null -> await(deferred)
                userSsoInfo != null -> handleUserSsoInfo(userSsoInfo)
                else -> activity?.finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode != REQUEST_CODE_GET_USER_SSO_INFO) return

        if (resultCode == Activity.RESULT_CANCELED) {
            activity?.finish()
            return
        }

        val result =
            data?.getParcelableExtraCompat<GetSsoInfoResult>(LoginSsoActivity.KEY_RESULT)

        if (result !is GetSsoInfoResult.Success) {
            activity?.finishWithResult(LoginSsoActivity.Result.Error.Unknown)
            return
        }

        handleUserSsoInfo(result.userSsoInfo)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(STATE_USER_SSO_INFO, userSsoInfo)
    }

    override fun onTermsAgreed(optInOffers: Boolean) {
        val userSsoInfo = userSsoInfo!!
        val deferred = deferredViewModel.async {
            provider.createAccount(
                login = userSsoInfo.login,
                ssoToken = userSsoInfo.ssoToken,
                serviceProviderKey = userSsoInfo.key,
                termsState = AccountCreator.TermsState(
                    conditions = true,
                    offers = optInOffers
                )
            )
        }
        await(deferred)
    }

    private fun handleUserSsoInfo(userSsoInfo: UserSsoInfo) {
        if (login != userSsoInfo.login) {
            activity?.finishWithResult(LoginSsoActivity.Result.Error.InvalidSso)
            return
        }

        this.userSsoInfo = userSsoInfo

        if (userSsoInfo.exists) {
            val deferred = deferredViewModel.async {
                provider.login(
                    login = userSsoInfo.login,
                    ssoToken = userSsoInfo.ssoToken,
                    serviceProviderKey = userSsoInfo.key
                )
            }
            await(deferred)
        } else {
            view.showTerms()
        }
    }

    private fun await(deferred: Deferred<Intent>) {
        fun finishWithError(e: LoginSsoActivity.Result.Error) {
            activity?.finishWithResult(e)
        }

        activityLifecycleCoroutineScope.launch(mainImmediateCoroutineDispatcher) {
            view.showLoading()
            try {
                val intent = deferred.await()
                notifySuccess(intent)
            } catch (_: CancellationException) {
                
            } catch (e: AuthenticationInvalidSsoException) {
                logger.logInvalidSso()
                finishWithError(LoginSsoActivity.Result.Error.InvalidSso)
            } catch (_: AuthenticationOfflineException) {
                finishWithError(LoginSsoActivity.Result.Error.Offline)
            } catch (_: AuthenticationNetworkException) {
                finishWithError(LoginSsoActivity.Result.Error.Network)
            } catch (_: AuthenticationExpiredVersionException) {
                finishWithError(LoginSsoActivity.Result.Error.ExpiredVersion)
            } catch (e: AuthenticationUnknownException) {
                logger.logErrorUnknown()
                finishWithError(LoginSsoActivity.Result.Error.Unknown)
            } catch (e: LoginSsoContract.CannotStartSessionException) {
                logger.logErrorUnknown()
                finishWithError(LoginSsoActivity.Result.Error.Unknown)
            }
        }
    }

    private fun notifySuccess(intent: Intent) {
        activity?.run {
            startActivity(intent)
            finishWithResult(LoginSsoActivity.Result.Success)
        }
    }

    companion object {
        private const val REQUEST_CODE_GET_USER_SSO_INFO = 32_487

        private const val STATE_USER_SSO_INFO = "user_sso_info"
    }
}

private fun Activity.finishWithResult(result: LoginSsoActivity.Result) {
    setResult(Activity.RESULT_OK, Intent().putExtra(LoginSsoActivity.KEY_RESULT, result))
    finish()
}
