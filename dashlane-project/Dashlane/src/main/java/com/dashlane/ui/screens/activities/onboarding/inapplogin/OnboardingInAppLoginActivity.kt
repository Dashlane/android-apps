package com.dashlane.ui.screens.activities.onboarding.inapplogin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import android.view.View
import androidx.core.app.NotificationManagerCompat
import androidx.viewpager2.widget.ViewPager2
import com.dashlane.R
import com.dashlane.inapplogin.InAppLoginByAutoFillApiManager.Companion.SET_AUTOFILL_PROVIDER_REQUEST_CODE
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.login.lock.LockManager
import com.dashlane.notification.creator.AutoFillNotificationCreator
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.screens.fragments.onboarding.inapplogin.OnboardingAccessibilityServices
import com.dashlane.ui.screens.fragments.onboarding.inapplogin.OnboardingInAppLoginDone
import com.dashlane.ui.screens.fragments.onboarding.inapplogin.OnboardingStep
import com.dashlane.ui.screens.fragments.onboarding.inapplogin.adapter.OnboardingInAppLoginFragmentStatePagerAdapter
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.dashlane.useractivity.log.usage.UsageLogCode95
import com.dashlane.util.getSerializableExtraCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingInAppLoginActivity : DashlaneActivity() {
    @Inject
    lateinit var lockManager: LockManager

    @Inject
    lateinit var inAppLoginManager: InAppLoginManager

    @Inject
    lateinit var teamspaceRepository: TeamspaceManagerRepository

    @Inject
    lateinit var sessionManager: SessionManager

    private var viewPager: ViewPager2? = null
    private var pagerAdapter: OnboardingInAppLoginFragmentStatePagerAdapter? = null
    private var callOrigin: String? = null

    private var onboardingType: OnboardingType = OnboardingType.AUTO_FILL_API

    private val logHelper: OnboardingInAppLoginUlHelper by lazy {
        OnboardingInAppLoginUlHelper(
            teamspaceRepository[session],
            UserActivityComponent(this).currentSessionUsageLogRepository
        )
    }

    private var isSettingAutofillLaunched = false

    private val session
        get() = sessionManager.session

    private val isEnableForCurrentOnBoarding: Boolean
        get() = if (onboardingType == OnboardingType.ACCESSIBILITY) {
            inAppLoginManager.isEnable(InAppLoginManager.TYPE_ACCESSIBILITY)
        } else {
            inAppLoginManager.isEnable(InAppLoginManager.TYPE_AUTO_FILL_API)
        }

    var userWentToAccessibilitySettings = false

    override fun onUserInteraction() {
        lockManager.setLastActionTimestampToNow()
        super.onUserInteraction()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding_in_app_login)
        viewPager = findViewById(R.id.activity_onboarding_in_app_login_viewpager)

        
        viewPager?.isUserInputEnabled = false

        intent?.let {
            callOrigin = it.getStringExtra(ORIGIN)
            onboardingType = it.getSerializableExtraCompat(EXTRA_ONBOARDING_TYPE) ?: OnboardingType.AUTO_FILL_API
        }

        if (UsageLogCode95.From.REMINDER_NOTIFICATION.code == callOrigin) {
            NotificationManagerCompat.from(this).cancel(AutoFillNotificationCreator.NOTIFICATION_ID)
        }

        if (session == null) {
            finish()
            return
        }
        logHelper.sendUsageLog34()

        if (savedInstanceState != null) {
            userWentToAccessibilitySettings = savedInstanceState.getBoolean(
                SAVED_USER_WENT_TO_ACCESSIBILITY_SETTINGS,
                false
            )
        }
        pagerAdapter =
            OnboardingInAppLoginFragmentStatePagerAdapter(supportFragmentManager, this.lifecycle)
        buildOnboardingSteps(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        when (onboardingType) {
            OnboardingType.ACCESSIBILITY -> handleAccessibilityFlow()
            OnboardingType.AUTO_FILL_API -> handleAutofillApiFlow()
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putInt(SAVED_STATE_CURRENT_POSITION, viewPager!!.currentItem)
        outState.putBoolean(
            SAVED_USER_WENT_TO_ACCESSIBILITY_SETTINGS,
            userWentToAccessibilitySettings
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != SET_AUTOFILL_PROVIDER_REQUEST_CODE) return

        
        
        
        
        
        if (resultCode == Activity.RESULT_OK && !isEnableForCurrentOnBoarding) {
            launchAutoFillSetting(inAppLoginManager, false)
        }
    }

    private fun buildOnboardingSteps(savedInstanceState: Bundle?) {
        pagerAdapter?.setOnboardingInAppLoginFragments(
            arrayOf(
                OnboardingAccessibilityServices.newInstance(onboardingType, callOrigin),
                OnboardingInAppLoginDone.newInstance(onboardingType, callOrigin)
            )
        )
        viewPager?.adapter = pagerAdapter
        if (savedInstanceState != null) {
            viewPager?.setCurrentItem(
                savedInstanceState.getInt(SAVED_STATE_CURRENT_POSITION),
                false
            )
        }
        viewPager?.isEnabled = false
    }

    private fun handleAccessibilityFlow() {
        viewPager?.visibility = View.VISIBLE
        when {
            isEnableForCurrentOnBoarding -> {
                userWentToAccessibilitySettings = false
                logHelper.sendUsageLog95(
                    UsageLogCode95.Action.SUCCESS,
                    callOrigin,
                    onboardingType.usageLog95Type
                )
                goToStep(OnboardingStep.CONFIRMATION, false)
            }
            userWentToAccessibilitySettings -> {
                userWentToAccessibilitySettings = false
                openDrawOnTopAuthorisationIfRequire()
            }
        }
    }

    private fun handleAutofillApiFlow() {
        when {
            
            
            (!isSettingAutofillLaunched) ->
                launchAutoFillSetting(inAppLoginManager, true)
            isSettingAutofillLaunched && isEnableForCurrentOnBoarding -> {
                viewPager?.visibility = View.VISIBLE
                goToStep(OnboardingStep.CONFIRMATION, false)
            }
            else -> finish()
        }
    }

    fun goToStep(step: OnboardingStep, smoothAnimate: Boolean) {
        viewPager?.setCurrentItem(step.stepValue, smoothAnimate)
            ?: return 
    }

    private fun openDrawOnTopAuthorisationIfRequire() {
        val intent = inAppLoginManager.intentOverlayPermissionIfRequire ?: return
        lockManager.startAutoLockGracePeriod()
        startActivity(intent)
    }

    private fun launchAutoFillSetting(
        inAppLoginManager: InAppLoginManager,
        toEnable: Boolean
    ) {
        isSettingAutofillLaunched = true
        lockManager.startAutoLockGracePeriod()

        val activityStarted = if (toEnable) {
            inAppLoginManager.inAppLoginByAutoFillApiManager
                ?.startActivityToChooseProviderForResult(this)
        } else {
            inAppLoginManager.inAppLoginByAutoFillApiManager
                ?.startActivityToDisableProviderForResult(this)
        }

        if (activityStarted != true) {
            
            val intent = Intent(Settings.ACTION_SETTINGS)
            startActivity(intent)
        }
    }

    companion object {
        const val ORIGIN = "origin"
        const val EXTRA_ONBOARDING_TYPE = "extra_onboarding_type"

        private const val SAVED_STATE_CURRENT_POSITION = "saved_state_current_position"
        private const val SAVED_USER_WENT_TO_ACCESSIBILITY_SETTINGS =
            "saved_user_went_to_accessibility_settings"
    }
}