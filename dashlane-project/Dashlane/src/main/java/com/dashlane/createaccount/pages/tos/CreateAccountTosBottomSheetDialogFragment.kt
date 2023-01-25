package com.dashlane.createaccount.pages.tos

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.net.toUri
import com.dashlane.Legal
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.ui.ExpandedBottomSheetDialogFragment
import com.dashlane.util.setCurrentPageView
import com.dashlane.util.setTextWithLinks

class CreateAccountTosBottomSheetDialogFragment : ExpandedBottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setCurrentPageView(AnyPage.ACCOUNT_CREATION_TERMS_SERVICES, fromAutofill = true)
        val view = inflater.inflate(R.layout.bottom_sheet_tos_account_creation, container, false)
        with(view) {
            val finishCta = findViewById<Button>(R.id.cta_agree_tos)!!
            val tosConditions = findViewById<CheckBox>(R.id.view_tos_conditions)!!
            tosConditions.setOnCheckedChangeListener { _, isChecked ->
                finishCta.isEnabled = isChecked
            }
            val tosConditionsText = findViewById<TextView>(R.id.view_tos_conditions_text)!!
            tosConditionsText.setOnClickListener { tosConditions.performClick() }
            initConditionsText(tosConditionsText)

            val tosOffers = findViewById<CheckBox>(R.id.view_tos_offers)!!
            tosOffers.isChecked = !(arguments?.getBoolean(KEY_EXPLICIT_OPTIN_REQUIRED) ?: true)

            finishCta.setOnClickListener {
                
                if (tosConditions.isChecked) {
                    AgreedTosEvent.ChannelHolder.of(requireActivity())
                        .channel
                        .trySend(AgreedTosEvent(optInOffers = tosOffers.isChecked))
                    dismiss()
                }
            }
        }
        return view
    }

    private fun initConditionsText(conditionView: TextView) {
        val context = context ?: return
        val termsOfServiceText =
            context.getString(R.string.create_account_tos_bottom_sheet_conditions_link_text_terms_of_service)
        val privacyPolicyText =
            context.getString(R.string.create_account_tos_bottom_sheet_conditions_link_text_privacy_policy)
        val conditionsText =
            context.getString(
                R.string.create_account_tos_bottom_sheet_conditions_template,
                termsOfServiceText,
                privacyPolicyText
            )
        val stringLinks = listOf(
            termsOfServiceText to Legal.URL_TERMS_OF_SERVICE.toUri(),
            privacyPolicyText to Legal.URL_PRIVACY_POLICY.toUri()
        )
        conditionView.setTextWithLinks(conditionsText.asRequiredText(), stringLinks, conditionView)
    }

    private fun String.asRequiredText() =
        SpannableString(this + requireContext().getString(R.string.create_account_tos_conditions_required_indicator)).apply {
            setSpan(StyleSpan(Typeface.BOLD), length - 1, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

    companion object {
        private const val KEY_EXPLICIT_OPTIN_REQUIRED = "require_explicit_optin"

        fun newInstance(requireExplicitOptin: Boolean = true) =
            CreateAccountTosBottomSheetDialogFragment().apply {
            arguments = Bundle().apply { putBoolean(KEY_EXPLICIT_OPTIN_REQUIRED, requireExplicitOptin) }
        }
    }
}