package com.dashlane.autofill.api.rememberaccount.view

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dashlane.autofill.api.R
import com.dashlane.autofill.api.util.formSourceIdentifier
import com.dashlane.autofill.api.viewallaccounts.view.AutofillViewAllItemsActivity.Companion.LINK_SERVICE_REQUEST_KEY
import com.dashlane.autofill.api.viewallaccounts.view.AutofillViewAllItemsActivity.Companion.LINK_SERVICE_SHOULD_AUTOFILL
import com.dashlane.autofill.api.viewallaccounts.view.AutofillViewAllItemsActivity.Companion.LINK_SERVICE_SHOULD_LINK
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.hermes.generated.definitions.Space
import com.dashlane.ui.VaultItemImageHelper
import com.dashlane.url.toUrlDomainOrNull
import com.dashlane.util.PackageUtilities
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.valueWithoutWww
import com.dashlane.vault.model.isSpaceItem
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.titleForList
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.model.urlForUI
import kotlinx.coroutines.launch

class AutofillLinkServiceViewProxy(
    private val fragment: AutofillLinkServiceFragment,
    view: View,
    private val viewModel: AutofillLinkServiceViewModel,
    private val logger: AutofillLinkServiceLogger
) {

    private val titleView: TextView = view.findViewById(R.id.title)
    private val descriptionView: TextView = view.findViewById(R.id.detail)
    private val vaultImageView: ImageView = view.findViewById(R.id.vault_image)
    private val vaultUrlView: TextView = view.findViewById(R.id.vault_url)
    private val vaultLoginView: TextView = view.findViewById(R.id.vault_login)

    init {
        logger.logShowLinkPage()
        view.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            logger.logLinkServiceRefused()
            finishWithResult(shouldAutofill = true, shouldLink = false)
        }
        view.findViewById<Button>(R.id.link_button).also {
            when (viewModel.getFormSource()) {
                is ApplicationFormSource -> {
                    it.text = fragment.getString(R.string.autofill_link_app)
                }
                is WebDomainFormSource -> {
                    it.text = fragment.getString(R.string.autofill_link_website)
                }
            }
            it.setOnClickListener {
                logPositiveButtonClicked()
                finishWithResult(shouldAutofill = true, shouldLink = true)
            }
        }

        fragment.apply {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.state.collect {
                        if (it is AutofillLinkServiceState.OnDataLoaded) {
                            val imageDrawable =
                                VaultItemImageHelper.getIconDrawableFromSummaryObject(requireContext(), it.item)
                            when (viewModel.getFormSource()) {
                                is ApplicationFormSource -> {
                                    val appName = PackageUtilities.getApplicationNameFromPackage(
                                        requireContext(),
                                        it.formSource.formSourceIdentifier
                                    ) ?: it.formSource.formSourceIdentifier
                                    showTitle(appName)
                                    showDescription(it.item.titleForList, appName)
                                }
                                is WebDomainFormSource -> {
                                    val url = it.formSource.formSourceIdentifier.toUrlDomainOrNull()?.valueWithoutWww()
                                        ?: it.formSource.formSourceIdentifier
                                    showTitle(url)
                                    showDescription(it.item.titleForList, url)
                                }
                            }
                            vaultUrlView.text = it.item.urlForUI()
                            vaultLoginView.text = it.item.loginForUi
                            vaultImageView.setImageDrawable(imageDrawable)
                        }
                    }
                }
            }
        }
    }

    private fun logPositiveButtonClicked() {
        viewModel.getItemSummary()?.let { itemSummary ->
            val isProfessionalSpace = itemSummary.isSpaceItem() && itemSummary.spaceId.isNotSemanticallyNull()
            logger.logLinkServiceAccepted(
                itemId = itemSummary.id,
                space = if (isProfessionalSpace) Space.PROFESSIONAL else Space.PERSONAL,
                itemUrl = itemSummary.urlForGoToWebsite,
                autoFillFormSource = viewModel.getFormSource()
            )
        }
    }

    fun finishWithResult(shouldAutofill: Boolean, shouldLink: Boolean) {
        fragment.setFragmentResult(
            LINK_SERVICE_REQUEST_KEY,
            bundleOf(
                LINK_SERVICE_SHOULD_AUTOFILL to shouldAutofill,
                LINK_SERVICE_SHOULD_LINK to shouldLink
            )
        )
    }

    private fun showTitle(serviceName: String) {
        val titleString = fragment.requireContext().getString(R.string.autofill_link_existing, serviceName)
        val startIndex = titleString.indexOf(serviceName)
        val spannable = SpannableStringBuilder(titleString)
        spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, startIndex + serviceName.length, 0)
        titleView.text = spannable
    }

    private fun showDescription(title: String?, serviceName: String) {
        val descriptionString = when (viewModel.getFormSource()) {
            is ApplicationFormSource -> fragment.requireContext().getString(
                R.string.autofill_link_existing_detail_app,
                title,
                serviceName
            )
            is WebDomainFormSource -> fragment.requireContext().getString(
                R.string.autofill_link_existing_detail,
                title,
                serviceName
            )
        }
        val spannable = SpannableStringBuilder(descriptionString)
        title?.let {
            val startIndex = descriptionString.indexOf(title)
            spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, startIndex + title.length, 0)
        }
        val startIndex = descriptionString.indexOf(serviceName)
        spannable.setSpan(StyleSpan(Typeface.BOLD), startIndex, startIndex + serviceName.length, 0)
        descriptionView.text = spannable
    }
}