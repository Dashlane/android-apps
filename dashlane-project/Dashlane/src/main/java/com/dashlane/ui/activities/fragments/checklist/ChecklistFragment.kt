package com.dashlane.ui.activities.fragments.checklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dashlane.R
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChecklistFragment : AbstractContentFragment() {

    private val viewModel by viewModels<ChecklistViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.fragment_checklist, container, false)
        ChecklistViewProxy(viewModel, view, lifecycle)
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(): AbstractContentFragment = ChecklistFragment()
    }
}