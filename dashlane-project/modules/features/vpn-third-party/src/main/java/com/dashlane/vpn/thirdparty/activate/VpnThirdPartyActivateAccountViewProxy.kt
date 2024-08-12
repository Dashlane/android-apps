package com.dashlane.vpn.thirdparty.activate

import android.content.Context
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.net.toUri
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dashlane.util.setTextWithLinks
import com.dashlane.vpn.thirdparty.R
import com.google.android.material.textfield.TextInputLayout
import com.skocken.presentation.viewproxy.BaseViewProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VpnThirdPartyActivateAccountViewProxy(
    fragment: Fragment,
    defaultEmail: String?,
    suggestions: List<String>?
) : BaseViewProxy<VpnThirdPartyActivateAccountContract.Presenter>(fragment),
    VpnThirdPartyActivateAccountContract.ViewProxy {

    private val confirmButton: Button =
        findViewByIdEfficient(R.id.vpn_third_party_activate_account_positive_button)!!
    private val termsCheckbox: CheckBox =
        findViewByIdEfficient(R.id.vpn_third_party_activate_account_terms_checkbox)!!
    private val emailLayout: TextInputLayout =
        findViewByIdEfficient(R.id.vpn_third_party_activate_account_layout)!!
    private val emailInput: AutoCompleteTextView =
        findViewByIdEfficient(R.id.vpn_third_party_activate_account_input)!!

    init {
        setupCheckbox()
        setupInput(fragment.requireContext(), defaultEmail, suggestions)
        findViewByIdEfficient<Button>(R.id.vpn_third_party_activate_account_learn_more_button)!!
            .setOnClickListener { presenter.onLearnMoreClicked() }
        confirmButton.setOnClickListener {
            fragment.lifecycleScope.launch(Dispatchers.Main) {
                if (presenter.isEmailValid(emailInput.text.toString())) {
                    presenter.onConfirmClicked(emailInput.text.toString())
                } else {
                    emailLayout.error =
                        context.getString(R.string.vpn_third_party_activate_account_email_error)
                }
            }
        }
    }

    private fun mayEnableConfirmButton() {
        confirmButton.isEnabled =
            termsCheckbox.isChecked && emailInput.text.toString().isNotEmpty()
    }

    private fun setupInput(context: Context, defaultEmail: String?, suggestions: List<String>?) =
        emailInput.apply {
            suggestions?.let {
                val adapter = ArrayAdapter(
                    context,
                    R.layout.autocomplete_textview_adapter,
                    R.id.listTextView,
                    it
                )
                setAdapter(adapter)
            }

            val clearButton = getDrawable(context, R.drawable.ic_action_clear_content_filled)!!
            doAfterTextChanged {
                mayEnableConfirmButton()
                if (text.isEmpty()) {
                    setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    setOnTouchListener(null)
                } else {
                    emailLayout.error = null
                    setCompoundDrawablesWithIntrinsicBounds(null, null, clearButton, null)
                    setOnTouchListener { _, event ->
                        performClick()
                        if (event.action == MotionEvent.ACTION_UP && event.x > width - paddingRight - clearButton.intrinsicWidth) {
                            text = null
                            true
                        } else {
                            false
                        }
                    }
                }
            }
            setText(defaultEmail)
        }

    private fun setupCheckbox() = termsCheckbox.apply {
        setTextWithLinks(
            R.string.vpn_third_party_activate_account_terms_of_service,
            listOf(
                R.string.vpn_third_party_activate_account_terms_link to "https://www.hotspotshield.com/terms/".toUri(),
                R.string.vpn_third_party_activate_account_policy_link to "https://www.aura.com/legal/privacy-policy".toUri()
            )
        )
        setOnCheckedChangeListener { _, _ -> mayEnableConfirmButton() }
    }
}