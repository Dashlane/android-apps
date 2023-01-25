package com.dashlane.autofill.api.emptywebsitewarning.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.emptywebsitewarning.EmptyWebsiteWarningContract
import com.dashlane.util.Toaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EmptyWebsiteWarningViewProxy @Inject constructor(
    private val emptyWebsiteWarningActivity: EmptyWebsiteWarningActivity,
    private val presenter: EmptyWebsiteWarningContract.Presenter,
    private val toaster: Toaster
) : EmptyWebsiteWarningContract.ViewProxy {

    private lateinit var website: String
    private lateinit var itemId: String

    private lateinit var titleView: TextView
    private lateinit var websiteTextView: TextView
    private lateinit var accountEmail: TextView
    private lateinit var accountName: TextView

    private lateinit var positiveCta: Button
    private lateinit var negativeCta: Button

    private val coroutineScope: CoroutineScope
        get() = emptyWebsiteWarningActivity

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View? {
        val view = inflater.inflate(
            R.layout.bottom_sheet_empty_website_warning_dialog_fragment, container,
            false
        )
        initView(view)
        return view
    }

    override fun updateView(context: Context, website: String, itemId: String) {
        this.website = website
        this.itemId = itemId
        coroutineScope.launch {
            val result = presenter.getAccountWrapper(itemId, website) ?: return@launch
            withContext(Dispatchers.Main) {
                titleView.text = context.getString(R.string.autofill_empty_website_title, result.title)
                websiteTextView.text = result.websiteSuggestion
                accountName.text = result.title
                accountEmail.text = result.login
            }
            presenter.onDisplay(website)
        }
    }

    private fun initView(view: View) {
        titleView = view.findViewById(R.id.title)
        websiteTextView = view.findViewById(R.id.website_suggestion)
        accountEmail = view.findViewById(R.id.email)
        accountName = view.findViewById(R.id.name)
        positiveCta = view.findViewById(R.id.positive_cta)
        positiveCta.setOnClickListener {
            onAddWebsiteSelected()
        }
        negativeCta = view.findViewById(R.id.negative_cta)
        negativeCta.setOnClickListener {
            onCancel()
        }
    }

    override fun onCancel() {
        presenter.onCancel(this.website)
        emptyWebsiteWarningActivity.onNoResult()
    }

    private fun onAddWebsiteSelected() {
        coroutineScope.launch {
            val result = presenter.updateAccountWithNewUrl(itemId, website)
            if (result == null) {
                handleError()
                return@launch
            }
            val toastMessage = emptyWebsiteWarningActivity.applicationContext?.getString(
                R.string.autofill_empty_website_toast,
                result.title
            )
            toaster.show(toastMessage, Toast.LENGTH_SHORT)
            emptyWebsiteWarningActivity.onAutofillResult(result)
        }
    }

    

    private fun handleError() {
        toaster.show(R.string.error, Toast.LENGTH_SHORT)
        emptyWebsiteWarningActivity.onNoResult()
    }
}