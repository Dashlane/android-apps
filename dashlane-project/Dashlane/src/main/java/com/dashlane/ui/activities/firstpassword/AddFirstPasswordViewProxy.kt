package com.dashlane.ui.activities.firstpassword

import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.dashlane.R
import com.dashlane.util.TextWatcherDsl
import com.dashlane.util.addTextChangedListener
import com.dashlane.util.computeStatusBarColor
import com.dashlane.util.getColorOnForToolbar
import com.dashlane.util.graphics.getDominantColorFromBorder
import com.dashlane.util.setContentTint
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.skocken.presentation.viewproxy.BaseViewProxy

class AddFirstPasswordViewProxy(private val activity: AppCompatActivity) :
    BaseViewProxy<AddFirstPassword.Presenter>(activity),
    AddFirstPassword.ViewProxy {

    private val editTextPassword = findViewByIdEfficient<EditText>(R.id.edit_text_password)!!
    private val buttonSecure = findViewByIdEfficient<Button>(R.id.button_secure)!!
    private val buttonSave = findViewByIdEfficient<Button>(R.id.button_save)!!
    private val editTextLogin = findViewByIdEfficient<EditText>(R.id.edit_text_login)!!
    private var autofillDemoPrompt: BottomSheetDialog? = null

    init {
        editTextPassword.requestFocus()

        val saveButtonEnableConditionWatcher: TextWatcherDsl.() -> Unit = {
            afterTextChanged {
                buttonSave.isEnabled = editTextPassword.text.isNotEmpty() && editTextLogin.text.isNotEmpty()
            }
        }

        editTextPassword.addTextChangedListener(saveButtonEnableConditionWatcher)
        editTextLogin.addTextChangedListener(saveButtonEnableConditionWatcher)

        buttonSecure.paintFlags = buttonSecure.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        buttonSecure.setOnClickListener {
            presenter.onButtonSecureClicked()
        }

        buttonSave.setOnClickListener {
            presenter.onButtonSaveClicked(editTextLogin.text.toString(), editTextPassword.text.toString())
        }
    }

    override fun displayAutofillDemoPrompt() = BottomSheetDialog(context).apply {
        setContentView(R.layout.bottom_sheet_autofill_demo_prompt)

        setCancelable(false)

        setOnShowListener {
            
            findViewById<View>(R.id.design_bottom_sheet)?.let {
                BottomSheetBehavior.from(it).peekHeight = it.height
                it.setBackgroundResource(R.drawable.bottom_sheet_top_rounded_background)
            }

            findViewById<Button>(R.id.button_return_home)?.setOnClickListener {
                presenter.onButtonReturnHomeClicked()
            }

            findViewById<Button>(R.id.button_try_demo)?.setOnClickListener {
                presenter.onButtonTryDemoClicked()
            }
        }
    }.also { autofillDemoPrompt = it }.show()

    override fun dismissAutofillDemoPrompt() {
        autofillDemoPrompt?.dismiss()
    }

    override fun setLogin(email: String) {
        editTextLogin.setText(email)
    }

    override fun setupToolbar(drawable: Drawable) {
        val toolbar = findViewByIdEfficient<Toolbar>(R.id.toolbar)!!
        val toolbarIcon = findViewByIdEfficient<ImageView>(R.id.toolbar_icon)!!
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.apply {
            title = ""
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
        toolbarIcon.setImageDrawable(drawable)
        val dominantColor = getDominantColorFromBorder(drawable)
        toolbar.setBackgroundColor(dominantColor)
        toolbar.setContentTint(context.getColorOnForToolbar(dominantColor))
        activity.window.statusBarColor = computeStatusBarColor(dominantColor)
    }
}