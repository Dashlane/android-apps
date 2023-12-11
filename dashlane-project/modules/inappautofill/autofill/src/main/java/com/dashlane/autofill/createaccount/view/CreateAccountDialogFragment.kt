package com.dashlane.autofill.createaccount.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.databinding.BottomSheetCreateAccountLayoutBinding
import com.dashlane.autofill.createaccount.domain.AutofillCreateAccountErrors
import com.dashlane.autofill.createaccount.domain.AutofillCreateAccountResultHandler
import com.dashlane.autofill.formdetector.model.AutoFillHintSummary
import com.dashlane.autofill.generatepassword.AutofillGeneratePasswordService
import com.dashlane.autofill.generatepassword.GeneratePasswordState
import com.dashlane.autofill.generatepassword.GeneratePasswordViewModel
import com.dashlane.autofill.generatepassword.GeneratePasswordViewProxy
import com.dashlane.autofill.navigation.getAutofillBottomSheetNavigator
import com.dashlane.bottomnavigation.NavigableBottomSheetDialogFragmentCanceledListener
import com.dashlane.bottomnavigation.NavigableBottomSheetFragment
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.teamspaces.adapter.SpinnerUtil
import com.dashlane.teamspaces.adapter.TeamspaceSpinnerAdapter
import com.dashlane.util.Toaster
import com.dashlane.util.addTextChangedListener
import com.dashlane.util.colorpassword.ColorTextWatcher
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateAccountDialogFragment :
    NavigableBottomSheetFragment,
    NavigableBottomSheetDialogFragmentCanceledListener,
    Fragment() {

    @Inject
    lateinit var toaster: Toaster

    @Inject
    lateinit var generatePasswordService: AutofillGeneratePasswordService

    private val viewModel: CreateAccountViewModel by viewModels()
    private val generatePasswordViewModel: GeneratePasswordViewModel by viewModels()

    lateinit var binding: BottomSheetCreateAccountLayoutBinding
    lateinit var generatePasswordViewProxy: GeneratePasswordViewProxy
    private val autofillBottomSheetNavigator by lazy {
        getAutofillBottomSheetNavigator()
    }
    private val resultHandler: AutofillCreateAccountResultHandler?
        get() = (activity as? AutofillCreateAccountResultHandler)
    private var isLoginDropdownOpen = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setCurrentPageView(AnyPage.AUTOFILL_EXPLORE_PASSWORDS_CREATE, fromAutofill = true)
        binding = BottomSheetCreateAccountLayoutBinding.inflate(inflater, container, false)
        generatePasswordViewProxy = GeneratePasswordViewProxy(
            binding.generateLayout,
            generatePasswordService.getPasswordGeneratorDefaultCriteria(),
            generatePasswordViewModel,
            viewLifecycleOwner
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        
        binding.generateLayout.password.transformationMethod = null

        ViewCompat.setAccessibilityHeading(binding.title, true)
        binding.login.addTextChangedListener {
            afterTextChanged {
                viewModel.updateCanSave(
                    binding.login.text.toString(),
                    binding.generateLayout.password.text.toString(),
                )
            }
        }
        binding.generateLayout.password.addTextChangedListener(object : ColorTextWatcher(requireContext()) {
            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                super.onTextChanged(charSequence, start, before, count)

                viewModel.updateCanSave(
                    binding.login.text.toString(),
                    binding.generateLayout.password.text.toString(),
                )
            }
        })
        binding.login.setOnDismissListener {
            handleSpaceDropDownChange(false)
        }
        binding.loginLayout.setEndIconOnClickListener {
            handleSpaceDropDownChange(!isLoginDropdownOpen)
        }
        collectCreateAccountState()
        collectGeneratePasswordState()
        binding.saveButton.setOnClickListener {
            viewModel.save(
                binding.website.text.toString(),
                binding.login.text.toString(),
                binding.generateLayout.password.text.toString(),
                binding.spaceSpinner.selectedItemPosition
            )
        }
        binding.backArrow.setOnClickListener { autofillBottomSheetNavigator.popStack() }
        if (autofillBottomSheetNavigator.hasVisiblePrevious()) {
            binding.backArrow.visibility = View.VISIBLE
            binding.dashlogo.visibility = View.GONE
        } else {
            binding.backArrow.visibility = View.GONE
            binding.dashlogo.visibility = View.VISIBLE
        }
        viewModel.getContentForWebsiteField()?.let { prefillWebsiteField(it) }
    }

    private fun collectCreateAccountState() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.initData()
                viewModel.uiState.collect { state ->
                    enableSave(state.data.canSave)
                    when (state) {
                        is AutofillCreateAccountState.Initial -> Unit
                        is AutofillCreateAccountState.AccountCreated -> {
                            
                            generatePasswordViewModel.saveGeneratedPasswordIfUsed(state.authentifiant)
                        }
                        is AutofillCreateAccountState.Cancelled -> resultHandler?.onCancel()
                        is AutofillCreateAccountState.Error -> handleErrors(state.error)
                        is AutofillCreateAccountState.InitSuggestions -> {
                            binding.website.setAdapter(
                                ArrayAdapter(
                                    requireContext(),
                                    R.layout.autocomplete_textview_adapter,
                                    state.suggestionLogins ?: emptyList()
                                )
                            )
                            binding.login.setAdapter(
                                ArrayAdapter(
                                    requireContext(),
                                    R.layout.autocomplete_textview_adapter,
                                    state.suggestionEmails ?: emptyList()
                                )
                            )
                            if (binding.login.text.isNullOrBlank()) {
                                binding.login.setText(state.suggestionEmails?.firstOrNull())
                            }

                            
                            if (state.data.teamSpace != null) {
                                binding.spaceSpinner.apply {
                                    visibility = View.VISIBLE
                                    adapter = TeamspaceSpinnerAdapter(activity, state.data.teamSpace)
                                    SpinnerUtil.enableSpinner(this)
                                }
                                binding.spaceLabel.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun collectGeneratePasswordState() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                if (binding.generateLayout.password.text.isNullOrBlank()) {
                    generatePasswordViewModel.generateNewPassword(
                        generatePasswordService.getPasswordGeneratorDefaultCriteria()
                    )
                }
                generatePasswordViewModel.uiState.collect { state ->
                    if (state is GeneratePasswordState.PasswordSavedToHistory) {
                        
                        resultHandler?.onFinishWithResult(state.authentifiant)
                    }
                }
            }
        }
    }

    private fun handleSpaceDropDownChange(visible: Boolean) {
        if (visible) {
            isLoginDropdownOpen = true
            binding.loginLayout.setEndIconDrawable(R.drawable.ic_arrow_collapse)
            binding.login.showDropDown()
        } else {
            isLoginDropdownOpen = false
            binding.loginLayout.setEndIconDrawable(R.drawable.ic_arrow_down)
            binding.login.dismissDropDown()
        }
    }

    private fun handleErrors(error: AutofillCreateAccountErrors) {
        when (error) {
            AutofillCreateAccountErrors.USER_LOGGED_OUT,
            AutofillCreateAccountErrors.DATABASE_ERROR -> {
                resultHandler?.onError(error)
            }
            AutofillCreateAccountErrors.INCOMPLETE -> {
                displayError(error.message)
            }
        }
    }

    private fun prefillWebsiteField(website: String) {
        binding.website.setText(website)
    }

    private fun enableSave(enable: Boolean) {
        binding.saveButton.isEnabled = enable
    }

    private fun displayError(message: String) {
        toaster.show(message, Toast.LENGTH_SHORT)
    }

    override fun onNavigableBottomSheetDialogCanceled() {
        viewModel.onCancel()
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
}
