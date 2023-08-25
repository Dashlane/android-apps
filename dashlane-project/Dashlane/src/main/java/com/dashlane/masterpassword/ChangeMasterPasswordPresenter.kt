package com.dashlane.masterpassword

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.dashlane.R
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.exception.NotLoggedInException
import com.dashlane.hermes.generated.definitions.ChangeMasterPasswordError
import com.dashlane.masterpassword.logger.ChangeMasterPasswordLogger
import com.dashlane.masterpassword.tips.MasterPasswordTipsActivity
import com.dashlane.navigation.NavigationUtils
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.passwordstrength.getShortTitle
import com.dashlane.security.DashlaneIntent
import com.dashlane.sync.cryptochanger.SyncCryptoChangerCryptographyException
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.ui.credential.passwordgenerator.StrengthLevelUpdater
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.DeviceUtils
import com.dashlane.util.getSerializableCompat
import com.skocken.presentation.presenter.BasePresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChangeMasterPasswordPresenter(
    private val passwordStrengthEvaluator: PasswordStrengthEvaluator,
    private val logger: ChangeMasterPasswordLogger,
    private val origin: ChangeMasterPasswordOrigin,
    private val warningDesktopShown: Boolean,
    private val changeMasterPasswordLogoutHelper: ChangeMasterPasswordLogoutHelper,
    coroutineScope: CoroutineScope
) : BasePresenter<ChangeMasterPasswordContract.DataProvider, ChangeMasterPasswordContract.View>(),
    ChangeMasterPasswordContract.Presenter {

    private var hasPlayedSuccessAnimation = false
    private val strengthLevelUpdater = StrengthLevelUpdater(coroutineScope)

    enum class Step {
        ENTER_NEW_PASSWORD,
        CONFIRM_NEW_PASSWORD,
        CHANGING_MASTER_PASSWORD,
        SUCCESS_SCREEN
    }

    private lateinit var currentStep: Step
    private lateinit var expectedPassword: ObfuscatedByteArray

    private val dashlaneActivity
        get() = (activity as DashlaneActivity)

    private lateinit var scope: CoroutineScope

    override fun onCreate(savedInstanceState: Bundle?) {
        scope = dashlaneActivity.lifecycleScope
        dashlaneActivity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = resources?.getString(R.string.change_master_password_activity_label)
        }

        if (savedInstanceState != null) {
            restoreState(savedInstanceState)
        } else {
            logger.logChangeMasterPasswordStart()
            initDefaultState()
        }

        
        
        collectProgressState(savedInstanceState == null)

        initCurrentStep()

        view.setOnPasswordChangeListener { password ->
            if (password.isEmpty()) {
                view.showStrengthLevel(false)
            } else if (currentStep == Step.ENTER_NEW_PASSWORD) {
                view.showStrengthLevel(true)
                strengthLevelUpdater.updateWith(passwordStrengthEvaluator, password.toString()) { strength ->
                    view.configureStrengthLevel(strength.getShortTitle(view.context), strength)
                }
            }
        }

        view.clearPassword()
    }

    private fun collectProgressState(clearChannel: Boolean) {
        scope.launch(Dispatchers.Main) {
            if (clearChannel) {
                provider.clearChannel()
            }
            provider.progressStateFlow.collect { progress ->
                when (progress) {
                    MasterPasswordChanger.Progress.Initializing ->
                        view.setProgress(0.0f)
                    MasterPasswordChanger.Progress.Downloading ->
                        view.setProgress(0.1f)
                    is MasterPasswordChanger.Progress.Ciphering ->
                        view.setProgress(0.1f + 0.7f * (progress.index.toFloat() / progress.total.toFloat()))
                    MasterPasswordChanger.Progress.Uploading ->
                        view.setProgress(0.8f)
                    MasterPasswordChanger.Progress.Confirmation ->
                        view.setProgress(0.9f)
                    MasterPasswordChanger.Progress.Completed.Success -> {
                        view.setProgress(1.0f)
                        currentStep = Step.SUCCESS_SCREEN
                        initCurrentStep()
                    }
                    is MasterPasswordChanger.Progress.Completed.Error -> {
                        val progressStep = progress.progress
                        if (progressStep == MasterPasswordChanger.Progress.Confirmation) {
                            
                            
                            showConfirmationStepErrorDialog()
                        } else {
                            showErrorDialog()
                        }

                        logError(progress.error, progressStep)
                    }
                }
            }
        }
    }

    private fun restoreState(savedInstanceState: Bundle) {
        hasPlayedSuccessAnimation = savedInstanceState.getBoolean(BUNDLE_HAS_PLAYED_SUCCESS_ANIMATION)
        currentStep = savedInstanceState.getSerializableCompat(BUNDLE_CURRENT_STEP)!!
        val savedPwd = savedInstanceState.getSerializableCompat<ObfuscatedByteArray>(BUNDLE_EXPECTED_PASSWORD)
        if (savedPwd != null) {
            expectedPassword = savedPwd
        }
    }

    private fun initDefaultState() {
        currentStep = Step.ENTER_NEW_PASSWORD

        if (origin is ChangeMasterPasswordOrigin.Recovery && !warningDesktopShown) {
            displayAccountRecoveryPopup()
        }
    }

    private fun displayAccountRecoveryPopup() {
        DialogHelper()
            .builder(context!!)
            .setMessage(R.string.account_recovery_reset_password_desc)
            .setTitle(R.string.account_recovery_reset_password_title)
            .setPositiveButton(R.string.account_recovery_reset_password_validation) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false).show()
    }

    private fun showErrorDialog() {
        val onDismissErrorDialog = if (origin.fromLogin) {
            DialogInterface.OnDismissListener {
                currentStep = Step.ENTER_NEW_PASSWORD
                initCurrentStep()
            }
        } else {
            null
        }

        showDialog(
            context!!.getString(R.string.change_master_password_pop_failure_title),
            context!!.getString(R.string.change_master_password_popup_failure_message),
            !origin.fromLogin, 
            onDismissErrorDialog
        )
    }

    private fun showConfirmationStepErrorDialog() {
        val onDismissErrorDialog = DialogInterface.OnDismissListener {
            NavigationUtils.logoutAndCallLoginScreen(context!!)
        }
        showDialog(
            context!!.getString(R.string.change_master_password_pop_success_title),
            context!!.getString(R.string.change_master_password_popup_success_message),
            true,
            onDismissErrorDialog
        )
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onNextClicked(password: CharSequence) {
        when (currentStep) {
            Step.ENTER_NEW_PASSWORD -> {
                strengthLevelUpdater.updateWith(passwordStrengthEvaluator, password.toString()) { strength ->
                    if (strength.score >= PasswordStrengthScore.SAFELY_UNGUESSABLE) {
                        currentStep = Step.CONFIRM_NEW_PASSWORD
                        expectedPassword = password.encodeUtf8ToObfuscated()
                        initCurrentStep()
                    } else {
                        logger.logChangeMasterPasswordError(ChangeMasterPasswordError.WEAK_PASSWORD_ERROR)
                        view.configureStrengthLevel(
                            view.context.getString(R.string.change_master_password_not_strong_enough),
                            strength
                        )
                    }
                }
            }
            Step.CONFIRM_NEW_PASSWORD -> {
                if (password.encodeUtf8ToObfuscated().use { it == expectedPassword }) {
                    logger.logClickConfirmPasswordStep()
                    currentStep = Step.CHANGING_MASTER_PASSWORD
                    initCurrentStep()
                    GlobalScope.launch(Dispatchers.Main) {
                        if (origin is ChangeMasterPasswordOrigin.Migration) {
                            provider.migrateToMasterPasswordUser(expectedPassword, origin.authTicket)
                        } else {
                            provider.updateMasterPassword(expectedPassword, origin)
                        }
                    }
                } else {
                    logger.logChangeMasterPasswordError(ChangeMasterPasswordError.PASSWORDS_DONT_MATCH)
                    view.showError(view.resources.getString(R.string.change_master_password_mismatch))
                }
            }
            else -> {
                
            }
        }
    }

    override fun onTipsClicked() {
        logger.logClickShowTips()
        val tipsIntent = Intent(context, MasterPasswordTipsActivity::class.java)
        context?.startActivity(tipsIntent)
    }

    override fun onBackPressed() = when (currentStep) {
        Step.ENTER_NEW_PASSWORD -> {
            logger.logChangeMasterPasswordCancel()
            if (origin.fromLogin) {
                changeMasterPasswordLogoutHelper.logout(activity!!)
            }
            false
        }
        Step.CONFIRM_NEW_PASSWORD -> {
            currentStep = Step.ENTER_NEW_PASSWORD
            initCurrentStep()
            true
        }
        Step.CHANGING_MASTER_PASSWORD -> true
        Step.SUCCESS_SCREEN -> true
    }

    private fun showDialog(
        title: String,
        message: String,
        finishOnPositiveButton: Boolean = true,
        onDismiss: DialogInterface.OnDismissListener? = null
    ) {
        DialogHelper()
            .builder(context!!)
            .setMessage(message)
            .setTitle(title)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                if (finishOnPositiveButton) {
                    dashlaneActivity.finish()
                }
            }
            .setOnDismissListener(onDismiss)
            .setCancelable(false).show()
    }

    private fun initCurrentStep() {
        when (currentStep) {
            Step.ENTER_NEW_PASSWORD -> {
                view.apply {
                    hideLoader()
                    showStrengthLevel(true)
                    showTipsButton(true)
                    setTitle(view.context.getString(R.string.change_master_password_create_title))
                    setNextButtonText(view.context.getString(R.string.change_master_password_next_step_button))
                    clearPassword()
                    if (::expectedPassword.isInitialized) {
                        setPassword(expectedPassword.decodeUtf8ToString())
                    }
                }
                logger.logDisplayEnterPasswordStep()
            }
            Step.CONFIRM_NEW_PASSWORD -> {
                view.apply {
                    showStrengthLevel(false)
                    showTipsButton(false)
                    setTitle(view.context.getString(R.string.change_master_password_confirm_title))
                    setNextButtonText(view.context.getString(R.string.change_master_password_confirm_button))
                    clearPassword()
                }
                logger.logDisplayConfirmPasswordStep()
            }
            Step.CHANGING_MASTER_PASSWORD -> {
                view.clearPassword()
                closeKeyboard()
                dashlaneActivity.supportActionBar?.hide()
                view.showLoader()
            }
            Step.SUCCESS_SCREEN -> {
                closeKeyboard()
                dashlaneActivity.supportActionBar?.hide()
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    view.displaySuccess(!hasPlayedSuccessAnimation)
                    hasPlayedSuccessAnimation = true

                    showDialog(
                        context!!.getString(R.string.change_master_password_pop_success_title),
                        context!!.getString(R.string.change_master_password_popup_success_message),
                        onDismiss = {
                            logger.logChangeMasterPasswordComplete()
                            val intent = if (origin is ChangeMasterPasswordOrigin.Migration) {
                                origin.successIntent
                            } else {
                                DashlaneIntent.newInstance(context, HomeActivity::class.java)
                            }
                            activity?.startActivity(intent)
                        }
                    )
                }
                logger.logDisplayPasswordChanged()
            }
        }
    }

    private fun closeKeyboard() {
        DeviceUtils.hideKeyboard(dashlaneActivity)
    }

    private fun logError(e: Exception, progress: MasterPasswordChanger.Progress?) {
        when {
            e is SyncCryptoChangerCryptographyException ->
                logger.logChangeMasterPasswordError(ChangeMasterPasswordError.DECIPHER_ERROR)
            e is NotLoggedInException ->
                logger.logChangeMasterPasswordError(ChangeMasterPasswordError.LOGIN_ERROR)
            e is MasterPasswordChanger.SyncFailedException ->
                logger.logChangeMasterPasswordError(ChangeMasterPasswordError.SYNC_FAILED_ERROR)
            progress == MasterPasswordChanger.Progress.Downloading ->
                logger.logChangeMasterPasswordError(ChangeMasterPasswordError.DOWNLOAD_ERROR)
            progress == MasterPasswordChanger.Progress.Uploading ->
                logger.logChangeMasterPasswordError(ChangeMasterPasswordError.UPLOAD_ERROR)
            progress == MasterPasswordChanger.Progress.Confirmation ->
                logger.logChangeMasterPasswordError(ChangeMasterPasswordError.CONFIRMATION_ERROR)
            else ->
                logger.logChangeMasterPasswordError(ChangeMasterPasswordError.UNKNOWN_ERROR)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable(BUNDLE_CURRENT_STEP, currentStep)
        if (::expectedPassword.isInitialized) {
            outState.putSerializable(BUNDLE_EXPECTED_PASSWORD, expectedPassword)
        }
        outState.putBoolean(BUNDLE_HAS_PLAYED_SUCCESS_ANIMATION, hasPlayedSuccessAnimation)
    }

    companion object {
        private const val BUNDLE_CURRENT_STEP = "current_step"
        private const val BUNDLE_EXPECTED_PASSWORD = "expected_password"
        private const val BUNDLE_HAS_PLAYED_SUCCESS_ANIMATION = "has_played_success_animation"
    }
}