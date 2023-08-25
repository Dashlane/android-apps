package com.dashlane.item.linkedwebsites

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.dashlane.R
import com.dashlane.databinding.ActivityLinkedWebsitesBinding
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.item.linkedwebsites.item.LinkedAppsViewModel
import com.dashlane.item.linkedwebsites.item.LinkedWebsitesViewModel
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class LinkedServicesActivity : DashlaneActivity() {

    private val viewModel by viewModels<LinkedServicesViewModel>()
    private val websitesViewModel by viewModels<LinkedWebsitesViewModel>()
    private val appsViewModel by viewModels<LinkedAppsViewModel>()
    private lateinit var linkedServicesViewProxy: LinkedServicesViewProxy

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setCurrentPageView(AnyPage.ITEM_CREDENTIAL_DETAILS_WEBSITES)
        val binding = ActivityLinkedWebsitesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        linkedServicesViewProxy = LinkedServicesViewProxy(this, viewModel, websitesViewModel, appsViewModel, binding)

        actionBarUtil.setup()
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.multi_domain_credentials_title)
            if (!viewModel.fromViewOnly) {
                setCloseIndicatorButton(this)
            }
        }
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                linkedServicesViewProxy.onClose(true)
            }
        }
        )
    }

    private fun setCloseIndicatorButton(actionBar: ActionBar) {
        var drawable = ContextCompat.getDrawable(this@LinkedServicesActivity, R.drawable.close_cross)
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(
                drawable.mutate(),
                this@LinkedServicesActivity.getColor(R.color.text_brand_standard)
            )
            actionBar.setHomeAsUpIndicator(drawable)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        linkedServicesViewProxy.createMenu(menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            linkedServicesViewProxy.onClose(true)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val SHOW_LINKED_SERVICES = 8965
        const val PARAM_ITEM_ID = "itemId"
        const val PARAM_FROM_VIEW_ONLY = "fromViewOnly"
        const val PARAM_ADD_NEW = "addNew"
        const val PARAM_TEMPORARY_WEBSITES = "temporaryWebsite"
        const val PARAM_TEMPORARY_APPS = "temporaryApps"
        const val PARAM_URL_DOMAIN = "urlDomain"
        const val RESULT_TEMPORARY_WEBSITES = "resultTemporaryWebsite"
        const val RESULT_TEMPORARY_APPS = "resultTemporaryApps"
        const val RESULT_DATA_SAVED = 56448
    }
}