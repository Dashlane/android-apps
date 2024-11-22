package com.dashlane.masterpassword

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import com.dashlane.R
import com.dashlane.listeners.edittext.NoLockEditTextWatcher
import com.dashlane.lock.LockManager
import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.passwordstrength.PasswordStrengthHorizontalIndicatorView
import com.dashlane.passwordstrength.textColorRes
import com.dashlane.util.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.skocken.presentation.viewproxy.BaseViewProxy
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ChangeMasterPasswordViewProxy(activity: Activity, lockManager: LockManager) :
    BaseViewProxy<ChangeMasterPasswordPresenter>(activity),
    ChangeMasterPasswordContract.View {

    private val scrollView = findViewByIdEfficient<ScrollView>(R.id.scroll_view)!!
    private val titleView = findViewByIdEfficient<TextView>(R.id.title)!!
    private val nextButton = findViewByIdEfficient<Button>(R.id.next_btn)!!
    private val tipsButton = findViewByIdEfficient<Button>(R.id.tips_btn)!!
    private val errorView = findViewByIdEfficient<TextView>(R.id.change_master_password_error)!!
    private val passwordEditText = findViewByIdEfficient<TextInputEditText>(R.id.password_edit_text)!!
    private val strengthLevelTextView = findViewByIdEfficient<TextView>(R.id.strength_level_textview)!!
    private val strengthProgress =
        findViewByIdEfficient<PasswordStrengthHorizontalIndicatorView>(R.id.password_strength_indicator)!!

    private val percentViewProxy =
        ChangeMasterPasswordProgressViewProxy(findViewByIdEfficient<View>(R.id.progress_process_percent_layout)!!)

    private var watcher: TextWatcher? = null

    private val passwordText: CharSequence
        get() = passwordEditText.text ?: ""

    init {
        nextButton.setOnClickListener {
            presenter.onNextClicked(passwordText)
        }

        tipsButton.setOnClickListener {
            presenter.onTipsClicked()
        }

        passwordEditText.addTextChangedListener(NoLockEditTextWatcher(lockManager))

        passwordEditText.addTextChangedListener {
            afterTextChanged {
                errorView.visibility = View.GONE
                nextButton.isEnabled = it.isNotEmpty()
            }
        }

        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH,
                EditorInfo.IME_ACTION_DONE,
                EditorInfo.IME_ACTION_GO,
                EditorInfo.IME_ACTION_UNSPECIFIED -> {
                    presenter.onNextClicked(passwordText)
                    true
                }
                else -> false
            }
        }
    }

    override fun setTitle(title: String) {
        titleView.text = title
    }

    override fun setNextButtonText(text: String) {
        nextButton.text = text
    }

    override fun showTipsButton(visible: Boolean) {
        tipsButton.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    override fun showStrengthLevel(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        strengthLevelTextView.visibility = visibility
        strengthProgress.visibility = visibility
    }

    override fun setOnPasswordChangeListener(onPasswordChange: (Editable) -> Unit) {
        if (watcher != null) {
            passwordEditText.removeTextChangedListener(watcher)
            watcher = null
        }

        watcher = passwordEditText.addTextChangedListener {
            afterTextChanged(onPasswordChange)
        }
    }

    override fun configureStrengthLevel(message: String, passwordStrength: PasswordStrength) {
        strengthLevelTextView.text = message
        strengthLevelTextView.setTextColor(context.getColor(passwordStrength.textColorRes))

        strengthProgress.apply {
            setPasswordStrength(passwordStrength)
        }
    }

    override fun clearPassword() {
        passwordEditText.setText("")
        errorView.visibility = View.GONE
    }

    override fun setPassword(password: String) {
        passwordEditText.setText(password)
        passwordEditText.setSelection(password.length)
    }

    override fun showError(error: String) {
        errorView.visibility = View.VISIBLE
        errorView.text = error
    }

    override fun showLoader() {
        percentViewProxy.showLoader()
        scrollView.visibility = View.GONE
    }

    override fun hideLoader() {
        percentViewProxy.hideLoader()
        scrollView.visibility = View.VISIBLE
    }

    override fun setProgress(progress: Float) {
        percentViewProxy.setProgress(progress)
    }

    override suspend fun displaySuccess(animate: Boolean) = suspendCoroutine<Unit> { continuation ->
        percentViewProxy.showSuccess(
            resources.getString(R.string.change_master_password_progress_success_message),
            animate
        ) {
            continuation.resume(Unit)
        }
    }
}