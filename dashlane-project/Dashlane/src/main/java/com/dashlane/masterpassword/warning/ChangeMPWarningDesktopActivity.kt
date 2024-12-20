package com.dashlane.masterpassword.warning

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.get
import com.dashlane.R
import com.dashlane.changemasterpassword.ChangeMasterPasswordOrigin
import com.dashlane.databinding.ActivityWarningBinding
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockPrompt
import com.dashlane.masterpassword.ChangeMasterPasswordActivity
import com.dashlane.masterpassword.ChangeMasterPasswordLogoutHelper
import com.dashlane.server.api.endpoints.premium.PremiumStatus.PremiumCapability.Capability
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.LockRepository
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.findContentParent
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangeMPWarningDesktopActivity : DashlaneActivity() {

    private lateinit var origin: ChangeMasterPasswordOrigin

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var logoutHelper: ChangeMasterPasswordLogoutHelper

    @Inject
    lateinit var userFeatureChecker: UserFeaturesChecker

    @Inject
    lateinit var lockRepository: LockRepository

    @ApplicationCoroutineScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    @MainCoroutineDispatcher
    @Inject
    lateinit var mainDispatcher: CoroutineDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extraOrigin = intent.getParcelableExtraCompat<ChangeMasterPasswordOrigin>(EXTRA_ORIGIN)

        if (extraOrigin == null) {
            finish()
            return
        }

        origin = extraOrigin

        setContentView(R.layout.activity_warning)

        ActivityWarningBinding.bind(findContentParent()[0])
            .initView()
    }

    private fun ActivityWarningBinding.initView() {
        topLeftButton.run {
            isEnabled = false
            visibility = View.INVISIBLE
        }

        negativeButton.setOnClickListener { onCancelButtonClicked() }
        positiveButton.setOnClickListener { onChangeMPClicked() }

        when {
            origin is ChangeMasterPasswordOrigin.Migration -> setTexts(
                titleRes = R.string.master_password_user_migration_warning_title,
                subTitleRes = R.string.master_password_user_migration_warning_message,
                positiveButtonRes = R.string.master_password_user_migration_warning_cta_positive,
                negativeButtonRes = R.string.master_password_user_migration_warning_cta_negative
            )
            userFeatureChecker.has(Capability.SYNC) -> setTexts(
                titleRes = R.string.change_mp_warning_desktop_title,
                subTitleRes = R.string.change_mp_warning_desktop_description
            )
            else -> setTexts(
                titleRes = R.string.change_mp_warning_desktop_nosync_title,
                subTitleRes = R.string.change_mp_warning_desktop_nosync_description
            )
        }
    }

    private fun ActivityWarningBinding.setTexts(
        @StringRes titleRes: Int,
        @StringRes subTitleRes: Int,
        @StringRes infoBoxRes: Int = R.string.login_account_recovery_key_change_mp_warning,
        @StringRes positiveButtonRes: Int = R.string.change_mp_warning_desktop_positive_button,
        @StringRes negativeButtonRes: Int = R.string.change_mp_warning_desktop_negative_button
    ) {
        textTitle.setText(titleRes)
        textSubtitle.setText(subTitleRes)
        warningInfobox.text = getString(infoBoxRes)
        warningInfobox.visibility = View.VISIBLE
        positiveButton.setText(positiveButtonRes)
        negativeButton.setText(negativeButtonRes)
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if (origin.fromLogin) {
            logoutHelper.logout(this)
        } else {
            finish()
        }
    }

    private fun onChangeMPClicked() {
        val changeMasterPasswordActivityIntent = ChangeMasterPasswordActivity.newIntent(this, origin, true)

        if (origin.fromLogin) {
            startActivity(changeMasterPasswordActivityIntent)
        } else {
            
            
            
            
            applicationScope.launch(mainDispatcher) {
                lockRepository
                    .getLockManager(sessionManager.session!!)
                    .showAndWaitLockActivityForReason(
                        context = this@ChangeMPWarningDesktopActivity,
                        reason = LockEvent.Unlock.Reason.WithCode(
                            UNLOCK_EVENT_CODE,
                            LockEvent.Unlock.Reason.WithCode.Origin.CHANGE_MASTER_PASSWORD
                        ),
                        lockPrompt = LockPrompt.ForSettings,
                    ).takeIf { lockEvent ->
                        lockEvent is LockEvent.Unlock &&
                            lockEvent.reason is LockEvent.Unlock.Reason.WithCode &&
                            (lockEvent.reason as LockEvent.Unlock.Reason.WithCode).requestCode == UNLOCK_EVENT_CODE
                    }?.let { startActivity(changeMasterPasswordActivityIntent) }
            }
        }
    }

    private fun onCancelButtonClicked() {
        if (origin.fromLogin) {
            logoutHelper.logout(this)
        } else {
            finish()
        }
    }

    companion object {
        private const val EXTRA_ORIGIN = "origin"
        private const val UNLOCK_EVENT_CODE = 8740

        @JvmStatic
        fun newIntent(context: Context, origin: ChangeMasterPasswordOrigin): Intent =
            Intent(context, ChangeMPWarningDesktopActivity::class.java)
                .putExtra(EXTRA_ORIGIN, origin)
    }
}