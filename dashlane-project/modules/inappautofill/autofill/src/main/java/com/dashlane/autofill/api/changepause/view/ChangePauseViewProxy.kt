package com.dashlane.autofill.api.changepause.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.changepause.ChangePauseContract
import com.dashlane.autofill.api.changepause.model.ChangePauseModel
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.util.Toaster

class ChangePauseViewProxy(
    private val fragment: ChangePauseFragment,
    private val presenter: ChangePauseContract.Presenter,
    private val changePauseViewTypeProviderFactory: ChangePauseViewTypeProviderFactory,
    private val toaster: Toaster,
    private val autoFillFormSource: AutoFillFormSource
) : ChangePauseContract.View {
    private lateinit var activity: FragmentActivity
    private lateinit var itemsListView: RecyclerView
    private lateinit var itemsAdapter: DashlaneRecyclerAdapter<ChangePauseViewTypeProviderFactory.PauseSetting>

    fun setContentView(inflater: LayoutInflater, container: ViewGroup?): View {
        activity = fragment.requireActivity()
        val view = inflater.inflate(R.layout.fragment_pause_switch, container, false)

        itemsListView = view.findViewById(R.id.pauseSettings)
        itemsAdapter = DashlaneRecyclerAdapter()
        itemsAdapter.setOnItemClickListener { _, _, _, _ ->
            presenter.onTogglePause(autoFillFormSource)
        }
        itemsListView.run {
            layoutManager = LinearLayoutManager(activity)
            adapter = itemsAdapter
        }

        return view
    }

    fun onResume() {
        presenter.onResume(autoFillFormSource)
    }

    override fun resumeAutofill(pauseModel: ChangePauseModel) {
        itemsAdapter.populateItems(
            listOf(
                changePauseViewTypeProviderFactory.create(pauseModel)
            )
        )
        if (!pauseModel.isPaused) {
            val autofillResumeMessage = when (pauseModel.autoFillFormSource) {
                is ApplicationFormSource ->
                    fragment.getString(
                        R.string.autofill_changepause_remove_pause_application_message,
                        pauseModel.autoFillFormSourceTitle
                    )
                is WebDomainFormSource ->
                    fragment.getString(
                        R.string.autofill_changepause_remove_pause_website_message,
                        pauseModel.autoFillFormSourceTitle
                    )
            }
            toaster.show(autofillResumeMessage, Toast.LENGTH_SHORT)
        }
    }

    override fun updatePause(pauseModel: ChangePauseModel) {
        val activity = fragment.activity as? ChangePauseFragmentContract ?: return
        activity.updateActionBarTitle(pauseModel.title)
        itemsAdapter.populateItems(
            listOf(
                changePauseViewTypeProviderFactory.create(pauseModel)
            )
        )
    }

    override fun openPauseDialog(autoFillFormSource: AutoFillFormSource) {
        getChangePauseFragmentContract()?.openPauseFormSourceDialog()
    }

    private fun getChangePauseFragmentContract(): ChangePauseFragmentContract? {
        return fragment.activity as? ChangePauseFragmentContract
    }

    override fun showErrorOnToggle() {
        toaster.show(R.string.autofill_changepause_remove_pause_error_message, Toast.LENGTH_SHORT)
    }

    override fun startLoading() {
        itemsListView.isEnabled = false
    }

    override fun stopLoading() {
        itemsListView.isEnabled = true
    }
}
