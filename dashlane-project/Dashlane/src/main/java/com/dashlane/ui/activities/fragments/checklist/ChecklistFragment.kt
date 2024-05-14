package com.dashlane.ui.activities.fragments.checklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.viewModels
import com.dashlane.databinding.FragmentChecklistBinding
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.m2w.M2WResult
import com.dashlane.m2w.M2wActivityResultContract
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChecklistFragment : AbstractContentFragment() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var m2wActivityResult: M2wActivityResultContract

    private val viewModel by viewModels<ChecklistViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        setCurrentPageView(AnyPage.HOME_ONBOARDING_CHECKLIST)
        val binding = FragmentChecklistBinding.inflate(inflater)
        val m2wResultLauncher: ActivityResultLauncher<Unit> =
            registerForActivityResult(m2wActivityResult) {
                if (it == M2WResult.SKIPPED) {
                    navigator.goToHome()
                }
            }
        ChecklistViewProxy(
            viewModel = viewModel,
            binding = binding,
            lifecycle = lifecycle,
            navigator = navigator,
            m2wResultLauncher = m2wResultLauncher
        )
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(): AbstractContentFragment = ChecklistFragment()
    }
}