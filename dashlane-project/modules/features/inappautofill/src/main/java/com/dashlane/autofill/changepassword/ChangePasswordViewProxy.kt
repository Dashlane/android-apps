package com.dashlane.autofill.changepassword

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.autofill.changepassword.domain.AutofillChangePasswordErrors
import com.dashlane.autofill.changepassword.domain.AutofillChangePasswordResultHandler
import com.dashlane.autofill.api.databinding.BottomSheetChangePasswordLayoutBinding
import com.dashlane.autofill.generatepassword.GeneratePasswordState
import com.dashlane.autofill.generatepassword.GeneratePasswordViewModel
import com.dashlane.autofill.navigation.AutofillBottomSheetNavigator
import com.dashlane.util.Toaster
import com.dashlane.util.colorpassword.ColorTextWatcher
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import kotlinx.coroutines.launch

class ChangePasswordViewProxy(
    private val binding: BottomSheetChangePasswordLayoutBinding,
    private val viewModel: ChangePasswordViewModel,
    private val generatePasswordViewModel: GeneratePasswordViewModel,
    private val toaster: Toaster,
    private val resultHandler: AutofillChangePasswordResultHandler?,
    private val defaultUsername: String?,
    viewLifecycleOwner: LifecycleOwner,
    autofillBottomSheetNavigator: AutofillBottomSheetNavigator,
) : ChangePasswordContract.ViewProxy {

    private val context: Context
        get() = binding.root.context

    private var oldAuthentifiant: VaultItem<SyncObject.Authentifiant>? = null
    private val passwordTextWatcher = object : ColorTextWatcher(context) {
        override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
            super.onTextChanged(charSequence, start, before, count)
            viewModel.updateCanUse(
                binding.login.selectedItem?.toString(),
                binding.generateLayout.password.text?.toString()
            )
        }
    }

    init {
        binding.generateLayout.password.addTextChangedListener(passwordTextWatcher)
        binding.saveButton.setOnClickListener {
            viewModel.useNewPassword(
                binding.login.selectedItem.toString(),
                binding.generateLayout.password.text?.toString()
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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.prefillLogin()
                viewModel.uiState.collect { state ->
                    enableUse(state.data.canUse)
                    when (state) {
                        is AutofillChangePasswordState.Initial -> Unit
                        is AutofillChangePasswordState.PrefillLogin -> {
                            prefillLogin(state.logins)
                        }
                        is AutofillChangePasswordState.PasswordChanged -> {
                            oldAuthentifiant = state.oldAuthentifiant
                            
                            generatePasswordViewModel.saveGeneratedPasswordIfUsed(state.authentifiant)
                        }
                        is AutofillChangePasswordState.Error -> handleErrors(state.error)
                        is AutofillChangePasswordState.Cancelled -> resultHandler?.onCancel()
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                generatePasswordViewModel.uiState.collect { state ->
                    if (state is GeneratePasswordState.PasswordSavedToHistory) {
                        
                        resultHandler?.onFinishWithResult(state.authentifiant, oldAuthentifiant!!)
                    }
                }
            }
        }
    }

    override fun prefillLogin(logins: List<String>) {
        binding.login.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, logins).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        defaultUsername?.let {
            val index = logins.indexOfFirst { it.equals(defaultUsername, ignoreCase = true) }
            if (index > -1) binding.login.setSelection(index)
        }
        binding.generateLayout.generatorConfiguration.let {
            generatePasswordViewModel.generateNewPassword(it.getConfiguration())
        }
        binding.login.selectedItem?.let {
            val value = it as? String
            viewModel.setDefaultCredential(value)
        }
    }

    private fun handleErrors(error: AutofillChangePasswordErrors) {
        when (error) {
            AutofillChangePasswordErrors.USER_LOGGED_OUT,
            AutofillChangePasswordErrors.DATABASE_ERROR,
            AutofillChangePasswordErrors.NO_MATCHING_CREDENTIAL -> {
                resultHandler?.onError(error)
            }
            AutofillChangePasswordErrors.INCOMPLETE -> displayError(context.getString(error.resId))
        }
    }

    override fun enableUse(enable: Boolean) {
        binding.saveButton.isEnabled = enable
    }

    override fun displayError(message: String) {
        toaster.show(message, Toast.LENGTH_SHORT)
    }
}