package com.dashlane.ui.activities.firstpassword.autofilldemo

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.dashlane.R
import com.dashlane.util.KeyboardVisibilityDetector
import com.skocken.presentation.viewproxy.BaseViewProxy

class AutofillDemoViewProxy(activity: AppCompatActivity) :
    BaseViewProxy<AutofillDemo.Presenter>(activity),
    AutofillDemo.ViewProxy {

    private val mainView = findViewByIdEfficient<ConstraintLayout>(R.id.autofill_demo_constraint_layout)!!
    private val scrollView = findViewByIdEfficient<ScrollView>(R.id.scroll_view)!!
    private val websiteIcon = findViewByIdEfficient<ImageView>(R.id.website_icon)!!
    private val editTextLogin = findViewByIdEfficient<EditText>(R.id.edit_text_login)!!
    private val editTextPassword = findViewByIdEfficient<EditText>(R.id.edit_text_password)!!
    private val autofillPreview = findViewByIdEfficient<CardView>(R.id.autofill_preview)!!
    private val buttonFinish = findViewByIdEfficient<Button>(R.id.cta_setup)!!

    init {
        hideAutofillSuggestion()
        mainView.setClickOrFocusListener {
            hideAutofillSuggestion()
        }

        
        KeyboardVisibilityDetector(activity, { scrollToPreviewBottom() }, null)

        val onClickOrFocus: (View) -> Unit = { view ->
            presenter.onEditTextFocusAcquired(view.id)
            scrollToPreviewBottom()
        }

        editTextLogin.setClickOrFocusListener(onClickOrFocus)
        editTextPassword.setClickOrFocusListener(onClickOrFocus)

        buttonFinish.setOnClickListener {
            presenter.onButtonFinishClicked()
        }
    }

    override fun showAutofillSuggestion(login: String?, url: String, editTextId: Int) {
        
        
        
        val anchorViewId = when (editTextId) {
            R.id.edit_text_login -> R.id.login
            R.id.edit_text_password -> R.id.password
            else -> return
        }

        autofillPreview.visibility = View.VISIBLE
        val constraintSet = ConstraintSet()
        constraintSet.clone(mainView)
        constraintSet.connect(R.id.autofill_preview, ConstraintSet.TOP, anchorViewId, ConstraintSet.BOTTOM)
        constraintSet.connect(R.id.autofill_preview, ConstraintSet.START, anchorViewId, ConstraintSet.START)
        constraintSet.connect(R.id.autofill_preview, ConstraintSet.END, anchorViewId, ConstraintSet.END)
        constraintSet.applyTo(mainView)

        autofillPreview.findViewById<TextView>(R.id.line1TextView).text = login
        autofillPreview.findViewById<TextView>(R.id.line2TextView).text = url
        val pauseItem = autofillPreview.findViewById<ViewGroup>(R.id.pause_item)
        pauseItem.findViewById<View>(R.id.actionText).visibility = View.GONE

        autofillPreview.setOnClickListener { presenter.onAutofillTriggered() }
    }

    override fun setWebsiteIcon(drawable: Drawable) {
        websiteIcon.setImageDrawable(drawable)
    }

    override fun setCredential(login: String?, password: String?) {
        editTextLogin.setText(login)
        editTextPassword.setText(password)
    }

    override fun hideAutofillSuggestion() {
        autofillPreview.visibility = View.GONE
    }

    override fun enableFinish() {
        buttonFinish.visibility = View.VISIBLE
    }

    private fun scrollToPreviewBottom() {
        scrollView.post {
            scrollView.smoothScrollTo(0, autofillPreview.bottom)
        }
    }

    private fun View.setClickOrFocusListener(block: (View) -> Unit) {
        setOnClickListener { block(this) }
        setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                block(v)
            }
        }
    }
}