package com.dashlane.ui.fragments

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.coroutineScope
import com.dashlane.R
import com.dashlane.databinding.FragmentPasswordGeneratorBinding
import com.dashlane.databinding.IncludeFragmentPasswordGeneratorHeaderBinding
import com.dashlane.design.component.compat.view.ButtonMediumView
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.hermes.LogRepository
import com.dashlane.navigation.Navigator
import com.dashlane.navigation.paywall.PaywallIntroType
import com.dashlane.password.generator.PasswordGeneratorCriteria
import com.dashlane.passwordgenerator.PasswordGeneratorWrapper
import com.dashlane.passwordgenerator.criteria
import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.passwordstrength.PasswordStrengthScore
import com.dashlane.passwordstrength.borderColorRes
import com.dashlane.passwordstrength.getStrengthDescription
import com.dashlane.passwordstrength.isSafeEnoughForSpecialMode
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.filter.CounterFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.SpecificDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.lock.DefaultLockFilter
import com.dashlane.storage.userdata.accessor.filter.space.NoSpaceFilter
import com.dashlane.ui.PasswordGeneratorConfigurationView
import com.dashlane.ui.PasswordGeneratorConfigurationView.ConfigurationChangeListener
import com.dashlane.ui.credential.passwordgenerator.PasswordGeneratorLogger
import com.dashlane.featureflipping.FeatureFlip
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.util.clipboard.ClipboardCopy
import com.dashlane.util.colorpassword.ColorTextWatcher
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.setProgressDrawablePrimaryTrack
import com.dashlane.utils.PasswordScrambler
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.lang.ref.WeakReference
import javax.inject.Inject

@Suppress("LargeClass")
@ObsoleteCoroutinesApi
@AndroidEntryPoint
class PasswordGeneratorFragment : BaseUiFragment(), ConfigurationChangeListener {

    @Inject
    lateinit var passwordGeneratorWrapper: PasswordGeneratorWrapper

    @Inject
    lateinit var userFeaturesChecker: UserFeaturesChecker

    @Inject
    lateinit var logRepository: LogRepository

    @Inject
    lateinit var dataCounter: DataCounter

    @Inject
    lateinit var clipboardCopy: ClipboardCopy

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var frozenStateManager: FrozenStateManager

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var passwordScrambler: PasswordScrambler

    private val eligibleToSpecialPrideMode: Boolean
        get() = userFeaturesChecker.has(FeatureFlip.SPECIAL_PRIDE_MODE)

    private var isAllowedToDisplayPreviouslyGenerated = false
    private var hasNavigationDrawer = false

    private lateinit var binding: FragmentPasswordGeneratorBinding

    private val header: IncludeFragmentPasswordGeneratorHeaderBinding
        get() = binding.passwordGeneratorHeader
    private val generatorConfiguration: PasswordGeneratorConfigurationView
        get() = binding.generatorConfiguration
    private val generatedPasswordView: TextView
        get() = header.generatedPassword
    private val passwordStrengthView: TextView
        get() = header.generatedPasswordStrength
    private val passwordStrengthBar: ProgressBar
        get() = header.passwordStrengthBar
    private val passwordStrengthSpecialModeBar: ProgressBar
        get() = header.passwordStrengthSpecialModeBar
    private val copyPasswordButton: ButtonMediumView
        get() = header.copyGeneratedPassword
    private val generatePasswordButton: ButtonMediumView
        get() = header.regeneratePassword

    private var toolbarRef = WeakReference<Toolbar?>(null)

    var passwordGenerationCallback: PasswordGenerationCallback? = null
    var log75Subtype: String? = null

    private val logger: PasswordGeneratorLogger
        get() = PasswordGeneratorLogger(logRepository)

    private var generatorActor =
        lifecycle.coroutineScope.actor(
            context = Dispatchers.Main,
            capacity = Channel.CONFLATED
        ) {
            consumeEach {
                
                delay(300)
                generatePassword(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPasswordGeneratorBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeClickListeners()
        initializeSpecialModeBar()
        updateHeaderWith(null, null)
        restoreDefaultPreferences(
            resources.getInteger(R.integer.password_generator_min_length_generated_password),
            resources.getInteger(R.integer.password_generator_max_length_generated_password)
        )
        
        binding.generatorConfiguration.listener = this
        binding.passwordGeneratorHeader.generatedPassword.addTextChangedListener(ColorTextWatcher(requireContext()))
        setHasOptionsMenu(true)
        setMenuVisibility(false)
        setFrozenStateIfRequired()
    }

    override fun onStart() {
        super.onStart()
        if (hasNavigationDrawer) {
            toolbarRef.get()?.elevation = 0f
        }
    }

    override fun onResume() {
        super.onResume()
        
        refreshPreviouslyGeneratedPasswordButton()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.password_history_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        passwordGenerationCallback?.showPreviouslyGenerated()
        return true
    }

    private suspend fun onPasswordGenerated(password: String, passwordStrength: PasswordStrengthScore?) {
        passwordGenerationCallback?.onPasswordGenerated()
        passwordScrambler.runScramble(password) { scrambledPassword ->
            generatedPasswordView.text = scrambledPassword
        }
        updateHeaderWith(password, passwordStrength)
        copyPasswordButton.isClickable = true
    }

    override fun onDigitSwitched(criteria: PasswordGeneratorCriteria) {
        saveAsDefaultPreferences(criteria)
        generatorActor.trySend(criteria)
    }

    override fun onLetterSwitched(criteria: PasswordGeneratorCriteria) {
        saveAsDefaultPreferences(criteria)
        generatorActor.trySend(criteria)
    }

    override fun onSymbolSwitched(criteria: PasswordGeneratorCriteria) {
        saveAsDefaultPreferences(criteria)
        generatorActor.trySend(criteria)
    }

    override fun onAmbiguousSwitched(criteria: PasswordGeneratorCriteria) {
        saveAsDefaultPreferences(criteria)
        generatorActor.trySend(criteria)
    }

    override fun onLengthUpdated(criteria: PasswordGeneratorCriteria, fromUser: Boolean) {
        saveAsDefaultPreferences(criteria)
        updateLengthValue(criteria, fromUser)
    }

    private fun parseArguments() {
        val arguments = arguments
        if (arguments != null) {
            isAllowedToDisplayPreviouslyGenerated = arguments.getBoolean(ARGS_ALLOW_PREVIOUSLY_GENERATED_DISPLAY)
            hasNavigationDrawer = arguments.getBoolean(ARGS_HAS_NAVIGATION_DRAWER, false)
        }
    }

    private fun initializeClickListeners() {
        generatePasswordButton.onClick = {
            generatorConfiguration.getConfiguration(null).let {
                generatorActor.trySend(it)
            }
        }
        copyPasswordButton.onClick = {
            if (frozenStateManager.isAccountFrozen) {
                navigator.goToPaywall(PaywallIntroType.FROZEN_ACCOUNT)
            } else {
                copyAndSaveGeneratedPassword(null, true)
            }
        }
    }

    private fun initializeSpecialModeBar() {
        passwordStrengthSpecialModeBar.let { passwordStrengthSpecialModeBar ->
            if (eligibleToSpecialPrideMode) {
                val success =
                    passwordStrengthSpecialModeBar.setProgressDrawablePrimaryTrack(R.drawable.password_strength_pride_flag_bar)
                passwordStrengthSpecialModeBar.isVisible = success
            } else {
                passwordStrengthSpecialModeBar.isVisible = false
            }
        }
    }

    private fun countGeneratedPassword(): Int {
        val filter = CounterFilter(
            SpecificDataTypeFilter(SyncObjectType.GENERATED_PASSWORD),
            NoSpaceFilter,
            DefaultLockFilter
        )
        return dataCounter.count(filter)
    }

    private suspend fun generatePassword(configuration: PasswordGeneratorCriteria) {
        logger.logPasswordGenerate(configuration)
        passwordStrengthView.animate()?.alpha(0.0f)
        copyPasswordButton.isClickable = false
        val result = passwordGeneratorWrapper.generatePassword(configuration)
        onPasswordGenerated(result.password, result.passwordStrength)
    }

    private fun updateHeaderWith(password: String?, strength: PasswordStrengthScore?) {
        val context = generatedPasswordView.context ?: return

        val (strengthTitle, color, progress) = if (strength == null) {
            Triple(null, getDefaultDominantColor(context), 0)
        } else {
            Triple(
                strength.getStrengthDescription(context),
                context.getColor(strength.borderColorRes),
                strength.percentValue
            )
        }
        
        generatedPasswordView.let { view ->
            view.text = password
            if (view.movementMethod == null) {
                view.movementMethod = ScrollingMovementMethod()
            }
        }
        
        passwordStrengthView.text = strengthTitle
        passwordStrengthView.animate()?.alpha(1.0f)
        passwordStrengthView.tag = strength
        val anim = StrengthBarAnimation(
            passwordStrengthBar,
            passwordStrengthBar.progress.toFloat(),
            progress.toFloat(),
            color
        )
        anim.duration = 300
        passwordStrengthBar.startAnimation(anim)
        
        if (eligibleToSpecialPrideMode) {
            val isSafeEnoughForSpecialMode = strength?.isSafeEnoughForSpecialMode == true
            val alpha = if (isSafeEnoughForSpecialMode) 1f else 0f
            val duration = if (isSafeEnoughForSpecialMode) 400L else 250L
            val startDelay = if (isSafeEnoughForSpecialMode) 300L else 0L
            passwordStrengthSpecialModeBar.animate()
                ?.alpha(alpha)
                ?.setDuration(duration)
                ?.setStartDelay(startDelay)
                ?.start()
        }
    }

    private fun setFrozenStateIfRequired() {
        if (!frozenStateManager.isAccountFrozen) return

        generatePasswordButton.isEnabled = false
        generatorConfiguration.enable = false
    }

    private fun getDefaultDominantColor(context: Context) = context.getThemeAttrColor(R.attr.colorPrimary)

    private fun updateLengthValue(configuration: PasswordGeneratorCriteria, fromUser: Boolean) {
        if (fromUser) {
            generatorActor.trySend(configuration)
        }
    }

    private fun restoreDefaultPreferences(minLength: Int, maxLength: Int) {
        val defaultConfig = passwordGeneratorWrapper.criteria
        generatorConfiguration.setNewConfiguration(minLength, maxLength, defaultConfig)
        generatorActor.trySend(defaultConfig)
    }

    private fun saveAsDefaultPreferences(criteria: PasswordGeneratorCriteria) {
        passwordGeneratorWrapper.criteria = criteria
    }

    fun setToolbarRef(toolbarRef: Toolbar?) {
        this.toolbarRef = WeakReference(toolbarRef)
    }

    fun refreshPreviouslyGeneratedPasswordButton() {
        setMenuVisibility(isAllowedToDisplayPreviouslyGenerated && countGeneratedPassword() > 0)
    }

    fun copyAndSaveGeneratedPassword(domain: String?, canCopyToClipboard: Boolean) {
        val password = generatedPasswordView.text?.toString() ?: return
        val savedGeneratedPassword = runBlocking {
            passwordGeneratorWrapper.saveToPasswordHistory(
                password,
                domain.orEmpty(),
                getString(R.string.empty_str)
            )
        }
        if (canCopyToClipboard) {
            clipboardCopy.copyToClipboard(data = password, sensitiveData = true)
        }
        val configuration = generatorConfiguration.getConfiguration(null)
        saveAsDefaultPreferences(configuration)

        if (passwordGenerationCallback != null) {
            val tag = passwordStrengthView.tag
            val passwordStrength = if (tag is PasswordStrength) tag else null
            passwordGenerationCallback?.passwordSaved(
                savedGeneratedPassword,
                passwordStrength
            )
        }
    }

    companion object {
        @JvmField
        val TAG = PasswordGeneratorFragment::class.java.name
        private const val ARGS_ALLOW_PREVIOUSLY_GENERATED_DISPLAY = "args_allow_previously_generated_display"
        private const val ARGS_HAS_NAVIGATION_DRAWER = "args_has_navigation_drawer"

        @JvmStatic
        fun newInstance(hasNavigationDrawer: Boolean): PasswordGeneratorFragment {
            val fragment = PasswordGeneratorFragment()
            val args = Bundle()
            
            args.putBoolean(ARGS_ALLOW_PREVIOUSLY_GENERATED_DISPLAY, true)
            args.putBoolean(ARGS_HAS_NAVIGATION_DRAWER, hasNavigationDrawer)
            fragment.arguments = args
            return fragment
        }
    }
}