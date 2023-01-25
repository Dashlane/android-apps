package com.dashlane.login.pages.password

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Space
import android.widget.Spinner
import android.widget.TextView
import com.dashlane.R
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.login.pages.LoginBaseSubViewProxy
import com.dashlane.login.pages.LoginSwitchAccountUtil
import com.dashlane.util.addOnFieldVisibilityToggleListener
import com.dashlane.util.addTextChangedListener
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.getThemeAttrResourceId
import com.dashlane.util.graphics.TextFitDrawable
import com.dashlane.util.toUpperCaseToDisplay
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM
import com.google.android.material.textfield.TextInputLayout.END_ICON_PASSWORD_TOGGLE
import kotlin.math.roundToInt

class LoginPasswordViewProxy(view: View) : LoginBaseSubViewProxy<LoginPasswordContract.Presenter>(view),
    LoginPasswordContract.ViewProxy {

    override val passwordText: CharSequence
        get() = passwordView.text

    private val passwordLayout = findViewByIdEfficient<TextInputLayout>(R.id.view_login_pw_layout)!!
    private val passwordView = findViewByIdEfficient<EditText>(R.id.view_login_pw)!!
    private val btnCancel = findViewByIdEfficient<Button>(R.id.btn_cancel)
    private val unlockTopicView = findViewByIdEfficient<TextView>(R.id.topic)
    private val useAnotherAccountSpinner = findViewByIdEfficient<Spinner>(R.id.view_login_email_header_spinner)!!
    private val emailHeaderView = findViewByIdEfficient<View>(R.id.view_login_email_header)!!

    private val passwordExplanation = findViewByIdEfficient<TextView>(R.id.view_password_explanation)!!

    override var isDialog: Boolean = false

    override var isAccountRecoveryAvailable = false

    private val bottomSheetDialog by lazy {
        BottomSheetDialog(context).apply {
            setContentView(R.layout.dialog_bottom_sheet_password_help)
            setOnShowListener {
                presenter.onShowLoginPasswordHelp()
                val v = findViewById<View>(R.id.design_bottom_sheet)
                if (v != null) {
                    
                    BottomSheetBehavior.from(v).peekHeight = v.height
                }
            }
            findViewById<View>(R.id.view_login_help)!!.apply { setOnClickListener { presenter.onClickLoginHelpRequested() } }
            findViewById<View>(R.id.view_login_forgot_password)!!.apply { setOnClickListener { presenter.onClickForgotPassword() } }
        }
    }

    init {
        root.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            val height = bottom - top
            hasEnoughHeightForHeader = height > minContentHeight
            root.post {
                
                initTopSpaceHeight()
                
                emailHeaderView.background = null
            }
        }

        
        
        btnCancel?.maxLines = 2
        btnCancel?.setOnClickListener {
            presenter.onCancelClicked()
        }

        if (passwordView.text.isNullOrEmpty()) passwordLayout.setForget()
        passwordView.addTextChangedListener {
            var wasEmpty = true

            afterTextChanged {
                
                showError(null)
            }

            onTextChanged { charSequence, _, _, _ ->
                val isEmpty = charSequence.isNullOrEmpty()

                if (isEmpty != wasEmpty) {
                    wasEmpty = isEmpty
                    if (isEmpty) {
                        passwordLayout.setForget()
                    } else {
                        passwordLayout.setPwdToggle()
                    }
                }
            }
        }
        passwordView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                presenter.onNextClicked()
                true
            } else {
                false
            }
        }

        passwordLayout.addOnFieldVisibilityToggleListener { presenter.onPasswordVisibilityToggle(it) }

        DeveloperUtilities.preFillPassword(passwordView)
    }

    override fun requestFocus() {
        passwordView.requestFocus()
    }

    override fun showPasswordHelp() {
        bottomSheetDialog.show()
    }

    override fun setUnlockTopic(topic: String?) {
        if (topic != null) {
            unlockTopicView?.text = topic
        } else {
            unlockTopicView?.setText(R.string.enter_masterpassword)
        }
    }

    override fun showSwitchAccount(loginHistory: List<String>) {
        useAnotherAccountSpinner.run {
            LoginSwitchAccountUtil.setupSpinner(this, email, loginHistory) {
                presenter.onClickChangeAccount(it)
            }
            visibility = View.VISIBLE
        }
    }

    override fun hideSwitchAccount() {
        useAnotherAccountSpinner.visibility = View.GONE
    }

    override fun showUnlockLayout(useUnlockLayout: Boolean) {
        unlockTopicView?.visibility = visibleOrGone(useUnlockLayout)
        btnCancel?.visibility = View.VISIBLE
        finishButton.setText(if (useUnlockLayout) R.string.fragment_lock_master_password_button_unlock else R.string.login_password_page_login_button)
        forceHideLogo = useUnlockLayout

        initTopSpaceHeight()
    }

    private fun initTopSpaceHeight() {
        val space = findViewByIdEfficient<Space>(R.id.top_space)
        space ?: return

        val spaceHeight = context.resources.getDimension(
            if (isDialog || !hasEnoughHeightForHeader) R.dimen.lock_scene_top_space_dialog
            else R.dimen.lock_scene_top_space
        ).roundToInt()

        if (space.layoutParams?.height != spaceHeight) {
            space.layoutParams = space.layoutParams?.apply {
                height = spaceHeight
            }
        }
    }

    override fun setCancelBtnText(string: String?) {
        btnCancel?.apply {
            text = string
            visibility = if (string != null) View.VISIBLE else View.GONE
        }
    }

    override fun setExplanation(string: String) {
        passwordExplanation.text = string
    }

    private fun visibleOrGone(visible: Boolean) = if (visible) View.VISIBLE else View.GONE

    private fun TextInputLayout.setForget() {
        
        val (textSize, typeface) = TextView(context)
            .apply { setTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceButton)) }
            .let { it.textSize to it.typeface }

        endIconMode = END_ICON_CUSTOM
        endIconDrawable = TextFitDrawable(
            text = resources.getString(R.string.login_password_forgot_button).toUpperCaseToDisplay(),
            textColor = context.getThemeAttrColor(R.attr.colorAccentOnPrimary),
            textSize = textSize,
            typeface = typeface
        )
        setEndIconOnClickListener {
            presenter.onClickForgotButton()
        }
    }

    private fun TextInputLayout.setPwdToggle() {
        endIconMode = END_ICON_PASSWORD_TOGGLE
    }
}