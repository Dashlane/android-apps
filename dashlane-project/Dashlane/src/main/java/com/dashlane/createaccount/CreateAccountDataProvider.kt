package com.dashlane.createaccount

import android.view.LayoutInflater
import com.dashlane.account.UserAccountInfo
import com.dashlane.authentication.AuthenticationException
import com.dashlane.authentication.AuthenticationExpiredVersionException
import com.dashlane.authentication.create.AccountCreator
import com.dashlane.createaccount.pages.CreateAccountBaseContract
import com.dashlane.createaccount.pages.choosepassword.CreateAccountChoosePasswordContract
import com.dashlane.createaccount.pages.choosepassword.CreateAccountChoosePasswordDataProvider
import com.dashlane.createaccount.pages.confirmpassword.CreateAccountConfirmPasswordContract
import com.dashlane.createaccount.pages.confirmpassword.CreateAccountConfirmPasswordDataProvider
import com.dashlane.createaccount.pages.email.CreateAccountEmailContract
import com.dashlane.createaccount.pages.email.CreateAccountEmailDataProvider
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.login.root.LoginContract
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.log.AttributionsLogDataProvider
import com.skocken.presentation.provider.BaseDataProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Provider

class CreateAccountDataProvider @Inject constructor(
    override val layoutInflater: LayoutInflater,
    @DefaultCoroutineDispatcher
    private val defaultCoroutineDispatcher: CoroutineDispatcher,
    private val createEmailDataProvider: Provider<CreateAccountEmailDataProvider>,
    private val createChoosePasswordDataProvider: Provider<CreateAccountChoosePasswordDataProvider>,
    private val createConfirmPasswordDataProvider: Provider<CreateAccountConfirmPasswordDataProvider>,
    private val logger: CreateAccountLogger,
    private val accountCreator: AccountCreator,
    private val createAccountSuccessIntentFactory: CreateAccountSuccessIntentFactory,
    private val attributionsLogDataProvider: AttributionsLogDataProvider
) : BaseDataProvider<LoginContract.Presenter>(), CreateAccountContract.DataProvider {

    override fun isExplicitOptinRequired(inEuropeanUnion: Boolean): Boolean {
        return inEuropeanUnion
    }

    override suspend fun createAccount(
        username: String,
        masterPassword: ObfuscatedByteArray,
        accountType: UserAccountInfo.AccountType,
        termsState: AccountCreator.TermsState,
        biometricEnabled: Boolean,
        resetMpEnabled: Boolean,
        country: String
    ) = withContext(defaultCoroutineDispatcher) {
        try {
            coroutineScope {
                
                val attributionLogDataDeferred = async {
                    attributionsLogDataProvider.getAttributionLogData()
                }

                accountCreator.createAccount(
                    username = username,
                    password = masterPassword,
                    accountType = accountType,
                    termsState = termsState,
                    biometricEnabled = biometricEnabled,
                    resetMpEnabled = resetMpEnabled,
                    country = country,
                )

                val advertisingInfo =
                    try {
                        withTimeout(1_000) { attributionLogDataDeferred.await() }
                    } catch (e: TimeoutCancellationException) {
                        null
                    }
                logger.logSuccess(advertisingInfo)
            }
        } catch (e: AccountCreator.CannotInitializeSessionException) {
            throw CreateAccountBaseContract.NetworkException(e)
        } catch (e: AuthenticationExpiredVersionException) {
            throw CreateAccountBaseContract.ExpiredVersionException(e)
        } catch (e: AuthenticationException) {
            logger.logError(e)
            throw CreateAccountBaseContract.NetworkException(e)
        }
    }

    override fun createSuccessIntent() = createAccountSuccessIntentFactory.createIntent()

    override fun createEmailDataProvider(): CreateAccountEmailContract.DataProvider {
        return createEmailDataProvider.get()
    }

    override fun createChoosePasswordDataProvider(username: String, isB2B: Boolean): CreateAccountChoosePasswordContract.DataProvider {
        return createChoosePasswordDataProvider.get().apply {
            this.username = username
            this.isB2B = isB2B
        }
    }

    override fun createConfirmPasswordDataProvider(
        username: String,
        password: ObfuscatedByteArray,
        inEuropeanUnion: Boolean,
        origin: String?,
        country: String
    ): CreateAccountConfirmPasswordContract.DataProvider {
        return createConfirmPasswordDataProvider.get()
            .also {
                it.username = username
                it.masterPassword = password
                it.inEuropeanUnion = inEuropeanUnion
            }
    }
}