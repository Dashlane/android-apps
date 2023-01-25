package com.dashlane.authenticator

import android.content.Intent
import android.os.Bundle
import com.dashlane.authenticator.util.SetUpAuthenticatorResultContract.SuccessResultContract.Input
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.util.getParcelableExtraCompat
import com.skocken.presentation.presenter.BasePresenter



class AuthenticatorResultIntro : DashlaneActivity() {

    private lateinit var presenter: IntroScreenContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_intro)
        val input = intent.extras?.getParcelable(EXTRA_INPUT) as? Input
        val name = input?.domain ?: getString(R.string.authenticator_default_account_name)
        val success = intent.extras?.getBoolean(EXTRA_SUCCESS)
        if (success == null) {
            finish()
            return
        }
        presenter = Presenter(name, success)
        presenter.setView(IntroScreenViewProxy(this))
    }

    private class Presenter(private val credentialName: String, private val success: Boolean) :
        BasePresenter<IntroScreenContract.DataProvider, IntroScreenContract.ViewProxy>(),
        IntroScreenContract.Presenter {

        override fun onViewChanged() {
            super.onViewChanged()
            view.apply {
                if (success) showSuccess() else showError()
            }
        }

        override fun onClickPositiveButton() {
            activity?.apply {
                setResult(RESULT_OK, Intent().apply {
                    putExtra(RESULT_INPUT, intent.getParcelableExtraCompat<Input>(EXTRA_INPUT))
                })
                finish()
            }
        }

        override fun onClickNegativeButton() {
            activity?.apply {
                setResult(RESULT_CANCELED)
                finish()
            }
        }

        override fun onClickNeutralButton() {
            
        }

        override fun onClickLink(position: Int, label: Int) {
            
        }

        private fun IntroScreenContract.ViewProxy.showError() {
            setImageResourceWithTint(R.drawable.ic_authenticator_error, R.color.text_danger_quiet)
            val title = resources.getString(
                R.string.authenticator_error_intro_title,
                credentialName
            )
            setTitle(title)
            setDescription(context.getString(R.string.authenticator_error_intro_body))
            setPositiveButton(R.string.authenticator_error_intro_positive_button)
            setNegativeButton(R.string.authenticator_error_intro_negative_button)
        }

        private fun IntroScreenContract.ViewProxy.showSuccess() {
            setImageResource(R.drawable.ic_authenticator_success)
            val title = resources.getString(
                R.string.authenticator_success_intro_title,
                credentialName
            )
            setTitle(title)
            setDescription(R.string.authenticator_success_intro_body)
            setPositiveButton(R.string.authenticator_success_intro_positive_button)
        }
    }

    companion object {
        const val EXTRA_SUCCESS = "extra_success"
        const val EXTRA_INPUT = "extra_input"

        const val RESULT_INPUT = "result_input"
    }
}