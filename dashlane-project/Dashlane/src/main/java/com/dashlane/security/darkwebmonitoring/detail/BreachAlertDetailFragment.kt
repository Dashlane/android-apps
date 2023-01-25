package com.dashlane.security.darkwebmonitoring.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BreachAlertDetailFragment : AbstractContentFragment() {

    @Inject
    lateinit var presenter: BreachAlertDetail.Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_breach_alert_detail, container, false)

        val viewProxy = BreachAlertDetailViewProxy(view, requireActivity().lifecycleScope)
        presenter.setView(viewProxy)

        return view
    }

    override fun onStart() {
        super.onStart()

        presenter.onStart()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }
}