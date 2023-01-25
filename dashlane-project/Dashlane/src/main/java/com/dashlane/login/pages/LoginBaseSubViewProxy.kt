package com.dashlane.login.pages

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.dashlane.R
import com.dashlane.util.getThemeAttrColor
import com.skocken.presentation.viewproxy.BaseViewProxy
import kotlin.properties.Delegates

abstract class LoginBaseSubViewProxy<T : LoginBaseContract.Presenter>(rootView: View) : BaseViewProxy<T>(rootView),
    LoginBaseContract.View {

    val root: ConstraintLayout = findViewByIdEfficient(R.id.view_login_root)!!

    protected val errorTextView = findViewByIdEfficient<TextView>(R.id.error_text_view)

    override var showProgress: Boolean by Delegates.observable(false) { _, oldValue, newValue ->
        if (newValue != oldValue) {
            showProgress(newValue)
        }
    }

    override var email by Delegates.observable<String?>(null) { _, _, value ->
        if (value != null) {
            emailView?.text = value
        }
    }

    private val logo: View = findViewByIdEfficient(R.id.logo)!!
    private val emailView: TextView? = findViewByIdEfficient(R.id.view_login_email_header)
    private val progressBar: ProgressBar = findViewByIdEfficient(R.id.view_login_progress)!!
    protected val finishButton: Button = findViewByIdEfficient(R.id.view_login_finish)!!
    protected val minContentHeight = resources.getDimensionPixelSize(R.dimen.login_content_min_height)

    var forceHideLogo = false
    var hasEnoughHeightForHeader: Boolean = false

    init {
        finishButton.setOnClickListener {
            presenter.onNextClicked()
        }

        root.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            val height = bottom - top
            hasEnoughHeightForHeader = height > minContentHeight
            
            root.post {
                logo.visibility = if (!forceHideLogo && hasEnoughHeightForHeader) View.VISIBLE else View.GONE
            }
        }
    }

    private fun hideFinish() {
        finishButton.run {
            isEnabled = false
            setTextColor(Color.TRANSPARENT)
        }
    }

    private fun showProgress(show: Boolean) {
        val finishEnabled = finishButton.text.isNotEmpty()
        if (show) {
            val progressTintAttrId = if (finishEnabled) {
                hideFinish()
                R.attr.colorOnSecondary
            } else {
                R.attr.colorOnPrimary
            }

            progressBar.indeterminateTintList = ColorStateList.valueOf(context.getThemeAttrColor(progressTintAttrId))
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
            if (finishEnabled) {
                showFinish()
            }
        }
    }

    private fun showFinish() {
        finishButton.run {
            isEnabled = true
            setTextColor(context.getThemeAttrColor(R.attr.colorOnSecondary))
        }
    }

    override fun showError(errorResId: Int, onClick: () -> Unit) {
        val error = if (errorResId == 0) null else context.getString(errorResId)
        showError(error, onClick)
    }

    override fun showError(error: CharSequence?, onClick: () -> Unit) {
        if (error.isNullOrEmpty()) {
            clearError()
        } else {
            errorTextView?.run {
                setTextColor(context.getThemeAttrColor(R.attr.colorError))
                visibility = View.VISIBLE
                text = error
            }
        }

        errorTextView?.setOnClickListener {
            onClick()
        }
    }

    private fun clearError() {
        errorTextView?.visibility = View.GONE
    }

    override fun prepareForTransitionEnd() {
    }

    override fun prepareForTransitionStart() {
        emailView?.background?.alpha = 0
    }

    override fun init(savedInstanceState: Bundle?) {
    }

    override fun onSaveInstanceState(outState: Bundle) {
    }
}