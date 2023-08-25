package com.dashlane.autofill.api.changepassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.common.AutofillApiGeneratePasswordComponent
import com.dashlane.autofill.api.internal.AutofillApiComponent
import com.dashlane.autofill.api.navigation.getAutofillBottomSheetNavigator
import com.dashlane.bottomnavigation.NavigableBottomSheetDialogFragmentCanceledListener
import com.dashlane.bottomnavigation.NavigableBottomSheetFragment

class BottomSheetChangePasswordDialogFragment :
    NavigableBottomSheetFragment,
    NavigableBottomSheetDialogFragmentCanceledListener,
    Fragment() {

    private lateinit var presenter: ChangePasswordPresenter

    private val coroutineScope = this.lifecycleScope

    private val changePasswordComponent by lazy(LazyThreadSafetyMode.NONE) {
        AutofillApiChangePasswordComponent(requireContext())
    }
    private val generatePasswordComponent by lazy(LazyThreadSafetyMode.NONE) {
        AutofillApiGeneratePasswordComponent(requireContext())
    }
    private val autofillApiComponent by lazy(LazyThreadSafetyMode.NONE) {
        AutofillApiComponent(requireContext())
    }
    private val args: BottomSheetChangePasswordDialogFragmentArgs by navArgs()

    private val website: String?
        get() = args.argsWebpage

    private val packageName: String?
        get() = args.argsPackageName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            changePasswordComponent.changePasswordLogger.apply {
                this.packageName = packageName
                this.domain = website
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.cloneInContext(activity).inflate(
            R.layout.bottom_sheet_change_password_layout,
            container,
            false
        )
        val generateLogger = generatePasswordComponent.generatePasswordLogger
        val service = changePasswordComponent.autofillUpdateAccountService
        val config = autofillApiComponent.autoFillchangePasswordConfiguration
        presenter = ChangePasswordPresenter(
            coroutineScope,
            changePasswordComponent.changePasswordLogger,
            generateLogger,
            website,
            packageName,
            userFeaturesChecker = generatePasswordComponent.userFeaturesChecker
        ).apply {
            setView(
                ChangePasswordViewProxy(
                    view,
                    config.filterOnUsername,
                    changePasswordComponent.toaster,
                    generatePasswordComponent.generatePasswordService.getPasswordGeneratorDefaultCriteria(),
                    getAutofillBottomSheetNavigator(),
                    generateLogger
                )
            )
            setProvider(
                ChangePasswordDataProvider(
                    website,
                    packageName,
                    service,
                    config,
                    generatePasswordComponent.generatePasswordService
                )
            )
            initSpecialMode()
            if (savedInstanceState != null) {
                lastGeneratedPassword = savedInstanceState.getString(LAST_GENERATED_PASSWORD)
            }
            initDialog()
        }
        return view
    }

    override fun onNavigableBottomSheetDialogCanceled() {
        presenter.onCancel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        
        outState.putString(LAST_GENERATED_PASSWORD, presenter.lastGeneratedPassword)
    }

    companion object {
        private const val LAST_GENERATED_PASSWORD = "last_generated_password"
    }
}
