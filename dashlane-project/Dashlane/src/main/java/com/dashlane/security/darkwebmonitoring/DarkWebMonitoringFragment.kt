package com.dashlane.security.darkwebmonitoring

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.dashlane.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DarkWebMonitoringFragment : AbstractContentFragment() {

    @Inject
    lateinit var presenter: DarkWebMonitoringContract.Presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_darkweb_monitoring, container, false)
        val viewProxy: DarkWebMonitoringContract.ViewProxy =
            DarkWebMonitoringViewProxy(view, requireActivity() as DashlaneActivity)
        presenter.setView(viewProxy)
        setHasOptionsMenu(true)
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        presenter.onCreateOptionsMenu(inflater, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return presenter.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        presenter.onViewVisible()
    }

    override fun onStop() {
        super.onStop()
        presenter.onViewHidden()
    }
}