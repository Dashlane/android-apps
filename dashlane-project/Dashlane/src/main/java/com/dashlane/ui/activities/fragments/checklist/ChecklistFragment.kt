package com.dashlane.ui.activities.fragments.checklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dashlane.databinding.FragmentChecklistBinding
import com.dashlane.session.SessionManager
import com.dashlane.ui.M2xIntentFactory
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChecklistFragment : AbstractContentFragment() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var m2xIntentFactory: M2xIntentFactory

    private val viewModel by viewModels<ChecklistViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding = FragmentChecklistBinding.inflate(inflater)
        ChecklistViewProxy(
            viewModel = viewModel,
            binding = binding,
            lifecycle = lifecycle,
            navigator = navigator,
            m2xIntentFactory = m2xIntentFactory
        )
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(): AbstractContentFragment = ChecklistFragment()
    }
}