package com.dashlane.autofill.api.createaccount

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.common.AutofillApiGeneratePasswordComponent
import com.dashlane.autofill.api.navigation.getAutofillBottomSheetNavigator
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.bottomnavigation.NavigableBottomSheetDialogFragmentCanceledListener
import com.dashlane.bottomnavigation.NavigableBottomSheetFragment
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.setCurrentPageView

class BottomSheetCreateAccountDialogFragment :
    NavigableBottomSheetFragment,
    NavigableBottomSheetDialogFragmentCanceledListener,
    Fragment() {

    private lateinit var presenter: CreateAccountPresenter

    private val coroutineScope = this.lifecycleScope

    private val createAccountComponent by lazy(LazyThreadSafetyMode.NONE) {
        AutofillApiCreateAccountComponent(requireContext())
    }
    private val generatePasswordComponent by lazy(LazyThreadSafetyMode.NONE) {
        AutofillApiGeneratePasswordComponent(requireContext())
    }
    private val args: BottomSheetCreateAccountDialogFragmentArgs by navArgs()

    private val website: String?
        get() = args.argsWebpage

    private val packageName: String?
        get() = args.argsPackageName

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setCurrentPageView(AnyPage.AUTOFILL_EXPLORE_PASSWORDS_CREATE, fromAutofill = true)
        val view = inflater.cloneInContext(activity).inflate(
            R.layout.bottom_sheet_create_account_layout,
            container,
            false
        )
        val generateLogger = generatePasswordComponent.generatePasswordLogger
        val service = createAccountComponent.autofillAccountCreationService
        val emailList = service.loadExistingLogins()
        val websiteList = service.getFamousWebsitesList()
        presenter = CreateAccountPresenter(
            website,
            packageName,
            coroutineScope,
            createAccountComponent.createAccountLogger,
            generateLogger,
            userFeaturesChecker = generatePasswordComponent.userFeaturesChecker
        ).apply {
            setView(
                CreateAccountViewProxy(
                    view,
                    createAccountComponent.toaster,
                    emailList,
                    websiteList,
                    generatePasswordComponent.generatePasswordService.getPasswordGeneratorDefaultCriteria(),
                    getAutofillBottomSheetNavigator(),
                    generateLogger
                )
            )
            setProvider(
                CreateAccountDataProvider(
                    generatePasswordComponent.generatePasswordService,
                    createAccountComponent.autofillAccountCreationService
                )
            )
            initSpecialMode()
            if (savedInstanceState == null) {
                initDialog()
            } else {
                lastGeneratedPassword = savedInstanceState.getString(LAST_GENERATED_PASSWORD)
            }
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

    data class CreateAccountDomainInfo(
        val webDomain: String? = null,
        val packageName: String? = null
    ) {
        constructor(summary: AutoFillHintSummary?) : this(
            summary?.webDomain?.takeIf { it.isNotSemanticallyNull() },
            summary?.packageName?.takeIf { summary.webDomain.isNullOrBlank() && it.isNotSemanticallyNull() }
        )
    }

    companion object {
        private const val LAST_GENERATED_PASSWORD = "last_generated_password"
    }
}

internal fun AutoFillHintSummary?.getDomainInfoForCreateAccount():
        BottomSheetCreateAccountDialogFragment.CreateAccountDomainInfo =
    BottomSheetCreateAccountDialogFragment.CreateAccountDomainInfo(this)
