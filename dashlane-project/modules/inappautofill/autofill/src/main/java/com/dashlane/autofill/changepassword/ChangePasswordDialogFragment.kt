package com.dashlane.autofill.changepassword

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dashlane.autofill.changepassword.domain.AutofillChangePasswordResultHandler
import com.dashlane.autofill.api.databinding.BottomSheetChangePasswordLayoutBinding
import com.dashlane.autofill.generatepassword.AutofillGeneratePasswordService
import com.dashlane.autofill.generatepassword.GeneratePasswordViewModel
import com.dashlane.autofill.generatepassword.GeneratePasswordViewProxy
import com.dashlane.autofill.navigation.getAutofillBottomSheetNavigator
import com.dashlane.bottomnavigation.NavigableBottomSheetDialogFragmentCanceledListener
import com.dashlane.bottomnavigation.NavigableBottomSheetFragment
import com.dashlane.util.Toaster
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChangePasswordDialogFragment :
    NavigableBottomSheetFragment,
    NavigableBottomSheetDialogFragmentCanceledListener,
    Fragment() {
    @Inject
    lateinit var toaster: Toaster

    @Inject
    lateinit var autoFillchangePasswordConfiguration: AutoFillChangePasswordConfiguration

    @Inject
    lateinit var generatePasswordService: AutofillGeneratePasswordService

    private val viewModel: ChangePasswordViewModel by viewModels()
    private val generatePasswordViewModel: GeneratePasswordViewModel by viewModels()

    private val resultHandler: AutofillChangePasswordResultHandler?
        get() = (activity as? AutofillChangePasswordResultHandler)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = BottomSheetChangePasswordLayoutBinding.inflate(inflater, container, false)
        ChangePasswordViewProxy(
            binding,
            viewModel,
            generatePasswordViewModel,
            toaster,
            resultHandler,
            autoFillchangePasswordConfiguration.filterOnUsername,
            viewLifecycleOwner,
            getAutofillBottomSheetNavigator()
        )
        GeneratePasswordViewProxy(
            binding.generateLayout,
            generatePasswordService.getPasswordGeneratorDefaultCriteria(),
            generatePasswordViewModel,
            viewLifecycleOwner
        )
        return binding.root
    }

    override fun onNavigableBottomSheetDialogCanceled() {
        viewModel.onCancel()
    }
}
