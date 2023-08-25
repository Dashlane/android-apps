package com.dashlane.authenticator.util

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import com.dashlane.authenticator.AuthenticatorBaseViewModelContract
import com.dashlane.authenticator.AuthenticatorIntro
import com.dashlane.authenticator.AuthenticatorMultipleMatchesResult
import com.dashlane.authenticator.AuthenticatorMultipleMatchesResult.Companion.EXTRA_INPUTS
import com.dashlane.authenticator.AuthenticatorResultIntro
import com.dashlane.authenticator.AuthenticatorResultIntro.Companion.EXTRA_INPUT
import com.dashlane.authenticator.AuthenticatorResultIntro.Companion.EXTRA_SUCCESS
import com.dashlane.authenticator.AuthenticatorResultIntro.Companion.RESULT_INPUT
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.util.SetUpAuthenticatorResultContract.SuccessResultContract.Input
import com.dashlane.navigation.Navigator
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.vault.model.isNotSemanticallyNull
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.titleForListNormalized
import com.dashlane.vault.model.urlDomain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class SetUpAuthenticatorResultContract : ActivityResultContract<Unit?, Otp?>() {

    private lateinit var successResultLauncher: ActivityResultLauncher<List<Input>>

    override fun createIntent(
        context: Context,
        input: Unit?
    ): Intent {
        return Intent(context, AuthenticatorIntro::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?) =
        intent?.getParcelableExtraCompat<Otp>(AuthenticatorIntro.RESULT_OTP)

    fun register(
        coroutineScope: CoroutineScope,
        activityResultCaller: ActivityResultCaller,
        navigator: Navigator,
        viewModel: AuthenticatorBaseViewModelContract
    ): ActivityResultLauncher<Unit?> {
        successResultLauncher =
            SuccessResultContract().register(activityResultCaller, navigator, viewModel)
        return activityResultCaller.registerForActivityResult(this) { otp ->
            coroutineScope.launch {
                handleOtpResult(viewModel, otp)
            }
        }
    }

    fun handleOtpResult(
        viewModel: AuthenticatorBaseViewModelContract,
        otp: Otp?
    ) {
        if (otp != null) {
            val issuerDomain = otp.issuer?.let { issuer ->
                viewModel.urlDomainRegistry.search(issuer).ifEmpty {
                    issuer.toUrlDomainOrNull()?.let { defaultDomain ->
                        listOf(defaultDomain)
                    }
                }
            }
            
            val matchingCredentialDomains = if (issuerDomain == null) {
                
                emptyList()
            } else {
                viewModel.getCredentials().filter { credential ->
                    issuerDomain.any { credential.urlDomain?.toUrlDomainOrNull() == it }
                }
            }
            
            
            val matchingCredentialsUsername =
                if (matchingCredentialDomains.size > 1 && otp.user.isNotSemanticallyNull()) {
                    matchingCredentialDomains
                        .filter { credential ->
                            credential.email == otp.user || credential.login == otp.user
                        }.takeIf { it.isNotEmpty() }
                } else {
                    null
                }
            val matchingCredentials = matchingCredentialsUsername ?: matchingCredentialDomains
            val otpDomain = issuerDomain?.firstOrNull()?.value ?: otp.issuer
            val inputs = matchingCredentials.map {
                Input(otp, otpDomain, it.id, it.titleForListNormalized, it.loginForUi)
            }
            when {
                
                matchingCredentials.size > 1 -> successResultLauncher.launch(inputs)
                
                matchingCredentials.isEmpty() ->
                    successResultLauncher.launch(listOf(Input(otp, otpDomain)))
                
                else -> {
                    val item = matchingCredentials.first()
                    successResultLauncher.launch(
                        listOf(
                            Input(
                                otp,
                                otpDomain,
                                item.id,
                                item.titleForListNormalized,
                                item.loginForUi
                            )
                        )
                    )
                }
            }
        }
    }

    class SuccessResultContract : ActivityResultContract<List<Input>, Input?>() {
        @Parcelize
        data class Input(
            val otp: Otp,
            val domain: String?,
            val itemId: String? = null,
            val itemTitle: String? = null,
            val itemUsername: String? = null
        ) : Parcelable

        override fun createIntent(context: Context, input: List<Input>) = if (input.size == 1) {
            Intent(context, AuthenticatorResultIntro::class.java).apply {
                val firstInput = input.first()
                putExtra(EXTRA_SUCCESS, true)
                putExtra(EXTRA_INPUT, firstInput)
            }
        } else {
            Intent(context, AuthenticatorMultipleMatchesResult::class.java).apply {
                putParcelableArrayListExtra(EXTRA_INPUTS, ArrayList(input))
            }
        }

        override fun parseResult(resultCode: Int, intent: Intent?) =
            intent?.getParcelableExtraCompat<Input>(RESULT_INPUT)

        fun register(
            activityResultCaller: ActivityResultCaller,
            navigator: Navigator,
            viewModel: AuthenticatorBaseViewModelContract
        ) =
            activityResultCaller.registerForActivityResult(SuccessResultContract()) { input ->
                input ?: return@registerForActivityResult
                if (input.itemId == null) {
                    navigator.goToCreateAuthentifiant(
                        sender = null,
                        url = input.domain ?: "",
                        otp = input.otp
                    )
                } else {
                    
                    viewModel.onOtpSetup(input.itemId, input.otp)
                }
                viewModel.onSuccessAddOtp(input.itemId, input.otp)
            }
    }
}