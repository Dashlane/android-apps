package com.dashlane.darkweb.ui.setup

import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.dashlane.darkweb.registration.ui.R
import com.dashlane.darkweb.registration.ui.databinding.ActivityDarkwebSetupMailBinding
import com.dashlane.darkweb.ui.result.DarkWebSetupResultActivity
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.DeviceUtils
import com.dashlane.util.addTextChangedListener
import com.dashlane.util.startActivity
import kotlinx.coroutines.launch

internal class DarkWebSetupMailViewProxy(
    private val activity: ComponentActivity,
    private val binding: ActivityDarkwebSetupMailBinding,
    private val viewModel: DarkWebSetupMailViewModelContract
) {
    private val limitReachedDialog = DialogHelper().builder(activity)
        .setMessage(activity.getString(R.string.darkweb_setup_mail_limit_reached))
        .setNegativeButton(activity.getString(R.string.darkweb_setup_button_close), null)
        .create()

    init {
        ViewCompat.setAccessibilityHeading(binding.viewDarkwebMailTitle, true)

        binding.viewDarkwebMailInput.run {
            threshold = 0

            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    DeviceUtils.hideKeyboard(v)
                    onNextPressed()
                    true
                } else {
                    false
                }
            }

            addTextChangedListener {
                onTextChanged { s, _, _, _ -> viewModel.onMailChanged(s.toString()) }
            }
        }

        binding.viewDarkwebMailNext.setOnClickListener { onNextPressed() }

        binding.viewDarkwebMailCancel.setOnClickListener { onCancelPressed() }

        activity.lifecycleScope.launch {
            binding.viewDarkwebMailInput.setAdapter(
                ArrayAdapter(
                    activity,
                    R.layout.autocomplete_textview_adapter,
                    viewModel.suggestions.await()
                )
            )
        }

        activity.lifecycleScope.launch {
            viewModel.state.collect { state ->
                limitReachedDialog.dismiss()
                binding.showProgress(false)
                binding.showError(-1)

                when (state) {
                    is DarkWebSetupMailState.Succeed -> launchResultActivity(state)
                    is DarkWebSetupMailState.InProgress -> binding.showProgress(true)
                    DarkWebSetupMailState.Canceled -> activity.finish()
                    DarkWebSetupMailState.Idle -> Unit
                    is DarkWebSetupMailState.Failed.LimitReached -> limitReachedDialog.show()
                    is DarkWebSetupMailState.Failed.Unknown -> binding.showError(R.string.darkweb_setup_mail_error)
                    is DarkWebSetupMailState.Failed.InvalidMail -> binding.showError(R.string.darkweb_setup_mail_invalid)
                    is DarkWebSetupMailState.Failed.EmptyMail -> binding.showError(R.string.darkweb_setup_mail_empty)
                }
            }
        }
    }

    private fun onNextPressed() {
        val mail = binding.viewDarkwebMailInput.text.toString()
        viewModel.onOptInClicked(mail)
    }

    private fun onCancelPressed() {
        viewModel.onCancel()
    }

    private fun launchResultActivity(succeed: DarkWebSetupMailState.Succeed) {
        
        activity.startActivity<DarkWebSetupResultActivity> {
            putExtra(DarkWebSetupMailActivity.INTENT_SIGN_UP_MAIL, succeed.mail)
        }
        activity.finish()
    }
}

private fun ActivityDarkwebSetupMailBinding.showProgress(show: Boolean) {
    viewDarkwebMailProgressBar.isVisible = show
    viewDarkwebMailNext.run {
        isEnabled = !show
        isInvisible = show
    }
}

private fun ActivityDarkwebSetupMailBinding.showError(@StringRes errorResId: Int) {
    viewDarkwebMailLayout.run {
        error = if (errorResId == -1) null else context.getString(errorResId)
    }
}