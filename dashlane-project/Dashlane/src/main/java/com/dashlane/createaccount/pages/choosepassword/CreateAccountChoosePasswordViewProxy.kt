package com.dashlane.createaccount.pages.choosepassword

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintSet
import com.dashlane.R
import com.dashlane.createaccount.pages.choosepassword.validator.PasswordValidationResultViewProxy
import com.dashlane.createaccount.pages.choosepassword.validator.zxcvbn.PasswordValidationResultByZxcvbnViewProxy
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.util.addOnFieldVisibilityToggleListener
import com.dashlane.util.addTextChangedListener
import com.dashlane.util.getRelativePosition
import com.google.android.material.textfield.TextInputLayout
import com.skocken.presentation.viewproxy.BaseViewProxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred

class CreateAccountChoosePasswordViewProxy(rootView: View, private val scope: CoroutineScope) :
    BaseViewProxy<CreateAccountChoosePasswordContract.Presenter>(rootView),
    CreateAccountChoosePasswordContract.ViewProxy {
    override val passwordText: CharSequence
        get() = passwordView.text

    private val passwordLayout
        get() = findViewByIdEfficient<TextInputLayout>(R.id.view_create_account_password_layout)!!
    private val passwordView
        get() = findViewByIdEfficient<EditText>(R.id.view_create_account_password)!!

    private val passwordCreateTitle
        get() = findViewByIdEfficient<TextView>(R.id.choose_password_title)!!

    private val btnTips
        get() = findViewByIdEfficient<Button>(R.id.choose_password_tips_button)!!

    private val rootView
        get() = presenter.rootView
    private val tipsView: View?
        get() = rootView.findViewById(R.id.tips_card_container)
    private val layoutInflater
        get() = LayoutInflater.from(context)

    init {
        btnTips.setOnClickListener {
            presenter.onShowTipsClicked()
        }

        passwordView.addTextChangedListener {
            afterTextChanged {
                if (!passwordLayout.error.isNullOrBlank()) {
                    
                    passwordLayout.error = null
                }
                presenter.onPasswordChanged(it)
            }
        }
        passwordView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                presenter.onNextClicked()
                true
            } else {
                false
            }
        }

        
        passwordLayout.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            tipsView?.let { constrainTipsView() }
        }

        passwordLayout.addOnFieldVisibilityToggleListener { presenter.onPasswordVisibilityToggle(it) }

        
        DeveloperUtilities.preFillPassword(passwordView)
    }

    override fun setTitle(@StringRes resId: Int) {
        passwordCreateTitle.setText(resId)
    }

    override fun showPasswordStrength(strengthDeferred: Deferred<PasswordStrength?>?) {
        if (strengthDeferred == null) {
            hideTips()
            return
        }

        
        val tipsView = (tipsView ?: inflateTipsView()).findViewById<ViewGroup>(R.id.card_container)
        show(
            tipsView,
            PasswordValidationResultByZxcvbnViewProxy(scope),
            strengthDeferred
        )
    }

    override fun showError(@StringRes errorResId: Int) {
        val error = passwordLayout.context.getString(errorResId)
        passwordLayout.error = null 
        passwordLayout.error = error
    }

    private fun show(
        tipsView: ViewGroup,
        passwordValidatorViewProxy: PasswordValidationResultViewProxy,
        strengthDeferred: Deferred<PasswordStrength?>
    ) {
        if (tipsView.findViewById<View>(passwordValidatorViewProxy.requiredViewId()) == null) {
            
            tipsView.removeAllViews() 
            
            LayoutInflater.from(tipsView.context)
                .inflate(passwordValidatorViewProxy.getIncludeLayout(), tipsView)
        }
        passwordValidatorViewProxy.show(tipsView, strengthDeferred)
    }

    private fun inflateTipsView(): View {
        return layoutInflater.inflate(R.layout.include_password_creation_tips, rootView).also {
            this.tipsView!!.apply {
                post {
                    constrainTipsView()
                    alpha = 0.0f
                    visibility = View.VISIBLE
                    animate().alpha(1.0f)
                }
            }
        }
    }

    private fun hideTips() {
        tipsView?.let {
            it.animate().alpha(0.0f).withEndAction { rootView.removeView(it) }
        }
    }

    

    private fun constrainTipsView() {
        val rootView = this.rootView
        val top = passwordLayout.getRelativePosition(rootView, View::getY)
        val bottom = (rootView.height - top).toInt()
        rootView.post {
            ConstraintSet().apply {
                clone(rootView)
                connect(
                    R.id.tips_card_container,
                    ConstraintSet.BOTTOM,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.BOTTOM,
                    bottom
                )
                applyTo(rootView)
            }
        }
    }
}