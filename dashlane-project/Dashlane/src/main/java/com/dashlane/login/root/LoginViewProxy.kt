package com.dashlane.login.root

import android.animation.TimeInterpolator
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.navigation.findNavController
import androidx.transition.AutoTransition
import androidx.transition.ChangeTransform
import androidx.transition.Fade
import androidx.transition.Scene
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.dashlane.R
import com.dashlane.debug.DaDaDa
import com.dashlane.login.LoginHostFragmentDirections
import com.dashlane.login.pages.LoginBaseContract
import com.dashlane.login.pages.authenticator.LoginDashlaneAuthenticatorContract
import com.dashlane.login.pages.authenticator.LoginDashlaneAuthenticatorPresenter
import com.dashlane.login.pages.authenticator.LoginDashlaneAuthenticatorViewProxy
import com.dashlane.login.pages.biometric.BiometricContract
import com.dashlane.login.pages.biometric.BiometricPresenter
import com.dashlane.login.pages.biometric.BiometricViewProxy
import com.dashlane.login.pages.email.LoginEmailContract
import com.dashlane.login.pages.email.LoginEmailPresenter
import com.dashlane.login.pages.email.LoginEmailViewProxy
import com.dashlane.login.pages.password.LoginPasswordContract
import com.dashlane.login.pages.password.LoginPasswordPresenter
import com.dashlane.login.pages.password.LoginPasswordViewProxy
import com.dashlane.login.pages.pin.PinLockContract
import com.dashlane.login.pages.pin.PinLockPresenter
import com.dashlane.login.pages.pin.PinLockViewProxy
import com.dashlane.login.pages.sso.SsoLockContract
import com.dashlane.login.pages.sso.SsoLockPresenter
import com.dashlane.login.pages.sso.SsoLockViewProxy
import com.dashlane.login.pages.token.LoginTokenContract
import com.dashlane.login.pages.token.LoginTokenPresenter
import com.dashlane.login.pages.token.LoginTokenViewProxy
import com.dashlane.login.pages.totp.LoginTotpContract
import com.dashlane.login.pages.totp.LoginTotpPresenter
import com.dashlane.login.pages.totp.LoginTotpViewProxy
import com.skocken.presentation.viewproxy.BaseViewProxy

class LoginViewProxy(rootView: View, private val dadada: DaDaDa) : BaseViewProxy<LoginContract.Presenter>(rootView), LoginContract.LoginViewProxy {
    private val root: FrameLayout = findViewByIdEfficient(R.id.view_login_root_container)!!

    override fun transitionTo(presenter: LoginBaseContract.Presenter) {
        transition(null, presenter)
    }

    override fun transition(from: LoginBaseContract.Presenter?, to: LoginBaseContract.Presenter) {
        
        val transition = when {
            from is LoginEmailPresenter && to is LoginTotpPresenter -> createEmailToTotpTransition()
            from is LoginTotpPresenter && to is LoginEmailPresenter -> createTotpToEmailTransition()
            from is LoginTotpPresenter && to is LoginPasswordPresenter -> createTotpToPasswordTransition()
            from is LoginPasswordPresenter && to is LoginTotpPresenter -> createPasswordToTotpTransition()
            from is LoginEmailPresenter && to is LoginPasswordPresenter -> createEmailToPasswordTransition()
            from is LoginPasswordPresenter && to is LoginEmailPresenter -> createPasswordToEmailTransition()
            from is LoginEmailPresenter && to is LoginTokenPresenter -> createEmailToTokenTransition()
            from is LoginTokenPresenter && to is LoginEmailPresenter -> createTokenToEmailTransition()
            from is LoginTokenPresenter && to is LoginPasswordPresenter -> createTokenToPasswordTransition()
            from is LoginPasswordPresenter && to is LoginTokenPresenter -> createPasswordToTokenTransition()
            else -> AutoTransition().also { it.duration = DEFAULT_DURATION }
        }

        
        transition?.addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition) {
                from?.getViewOrNull()?.prepareForTransitionEnd()
                to.getViewOrNull()?.prepareForTransitionEnd()
                from?.visible = false
            }

            override fun onTransitionResume(transition: Transition) {
            }

            override fun onTransitionPause(transition: Transition) {
            }

            override fun onTransitionCancel(transition: Transition) {
            }

            override fun onTransitionStart(transition: Transition) {
                from?.getViewOrNull()?.prepareForTransitionStart()
                to.getViewOrNull()?.prepareForTransitionStart()
            }
        })

        val (viewProxy, scene) = createViewProxyForPresenter(to)
        to.setView(viewProxy)
        to.visible = true
        
        TransitionManager.go(scene, transition)
    }

    override fun transitionToCompose(email: String?) {
        root.findNavController().navigate(LoginHostFragmentDirections.actionFragmentToCompose(email))
    }

    private fun createViewProxyForPresenter(presenter: LoginBaseContract.Presenter) = when (presenter) {
        is LoginEmailPresenter -> createLoginEmailViewProxy()
        is LoginTotpPresenter -> createLoginTotpViewProxy()
        is LoginPasswordPresenter -> createLoginPasswordViewProxy()
        is LoginTokenPresenter -> createLoginTokenViewProxy()
        is LoginDashlaneAuthenticatorPresenter -> createLoginAuthenticatorPresenter()
        is BiometricPresenter -> createBiometricViewProxy()
        is PinLockPresenter -> createPinLockViewProxy()
        is SsoLockPresenter -> createSsoLockViewProxy()
        else -> throw IllegalArgumentException(
            "This LoginBaseContract.Presenter implementation is not associated to any ViewProxy"
        )
    }

    private fun inflate(@LayoutRes layoutResId: Int): View =
        LayoutInflater.from(root.context).inflate(layoutResId, root, false)

    private fun createLoginEmailViewProxy(): Pair<LoginEmailContract.ViewProxy, Scene> {
        val view = inflate(R.layout.scene_login_email)
        return LoginEmailViewProxy(view) to Scene(root, view)
    }

    private fun createLoginTokenViewProxy(): Pair<LoginTokenContract.ViewProxy, Scene> {
        val view = inflate(R.layout.scene_login_token)
        return LoginTokenViewProxy(view, dadada) to Scene(root, view)
    }

    private fun createLoginAuthenticatorPresenter(): Pair<LoginDashlaneAuthenticatorContract.ViewProxy, Scene> {
        val view = inflate(R.layout.scene_login_dashlane_authenticator)
        return LoginDashlaneAuthenticatorViewProxy(view) to Scene(root, view)
    }

    private fun createLoginTotpViewProxy(): Pair<LoginTotpContract.ViewProxy, Scene> {
        val view = inflate(R.layout.scene_login_totp)
        return LoginTotpViewProxy(view) to Scene(root, view)
    }

    private fun createLoginPasswordViewProxy(): Pair<LoginPasswordContract.ViewProxy, Scene> {
        val view = inflate(R.layout.scene_login_password)
        return LoginPasswordViewProxy(view) to Scene(root, view)
    }

    private fun createBiometricViewProxy(): Pair<BiometricContract.ViewProxy, Scene> {
        val view = inflate(R.layout.scene_login_biometric)
        return BiometricViewProxy(view) to Scene(root, view)
    }

    private fun createPinLockViewProxy(): Pair<PinLockContract.ViewProxy, Scene> {
        val view = inflate(R.layout.fragment_lock_pincode_forwarder)
        return PinLockViewProxy(view) to Scene(root, view)
    }

    private fun createSsoLockViewProxy(): Pair<SsoLockContract.ViewProxy, Scene> {
        val view = inflate(R.layout.scene_login_sso)
        return SsoLockViewProxy(view) to Scene(root, view)
    }
}

private const val DEFAULT_DURATION = 400L

private fun createEmailToTotpTransition(): Transition = TransitionSet()
    .addTransition(
        Fade().apply {
        addTarget(R.id.view_login_email_layout)
        duration = 400
        interpolator = bezier
    }
    )
    .addTransition(
        transformEmail(500).apply {
        startDelay = 250
    }
    )
    .addTransition(
        Fade().apply {
        addTarget(R.id.frame_login_totp)
        addTarget(R.id.btn_recovery)
        interpolator = bezier
        startDelay = 400
        duration = 400
    }
    )
    .addTransition(
        buttonChanger(R.id.btn_create_account, R.id.btn_push)
    )

private fun createTotpToEmailTransition(): Transition = TransitionSet()
    .addTransition(
        transformEmail(500).apply {
            startDelay = 50
        }
    )
    .addTransition(
        fadeOutSlide(listOf(R.id.frame_login_totp)).apply {
            duration = 400
        }
    )
    .addTransition(
        Fade().apply {
        addTarget(R.id.view_login_email_layout)
        duration = 400
        startDelay = 400
        interpolator = bezier
    }
    )
    .addTransition(
        buttonChanger(R.id.btn_push, R.id.btn_create_account)
    )

private fun createTotpToPasswordTransition(): Transition = TransitionSet()
    .addTransition(
        fadeOutSlide(listOf(R.id.frame_login_totp)).apply {
            duration = 400
        }
    )
    .addTransition(
        Fade().apply {
        addTarget(R.id.view_login_pw_layout)
        duration = 400
        startDelay = 400
        interpolator = bezier
    }
    )

private fun createPasswordToTotpTransition(): Transition = TransitionSet()
    .addTransition(
        Fade().apply {
        addTarget(R.id.view_login_pw_layout)
        interpolator = bezier
        duration = 400
    }
    )
    .addTransition(
        Fade().apply {
        addTarget(R.id.frame_login_totp)
        interpolator = bezier
        startDelay = 400
        duration = 400
    }
    )

private fun createEmailToPasswordTransition(): Transition = TransitionSet()
    .addTransition(
        Fade().apply {
        addTarget(R.id.view_login_email_layout)
        duration = 400
        interpolator = bezier
    }
    )
    .addTransition(
        transformEmail(500).apply {
            startDelay = 250
        }
    )
    .addTransition(
        Fade().apply {
        addTarget(R.id.view_login_pw_layout)
        interpolator = bezier
        startDelay = 400
        duration = 400
    }
    )

private fun createPasswordToEmailTransition(): Transition = TransitionSet()
    .addTransition(
        transformEmail(500).apply {
            startDelay = 50
        }
    )
    .addTransition(
        Fade().apply {
        addTarget(R.id.view_login_pw_layout)
        duration = 400
        interpolator = bezier
    }
    )
    .addTransition(
        fadeInSlide().apply {
        addTarget(R.id.view_login_email_layout)
        startDelay = 400
        duration = 400
    }
    )

private fun createTokenToPasswordTransition(): Transition = TransitionSet()
    .addTransition(
        fadeOutSlide(listOf(R.id.view_login_token_layout)).apply {
            duration = 400
        }
    )
    .addTransition(
        fadeInSlide().apply {
        addTarget(R.id.view_login_pw_layout)
        duration = 400
        startDelay = 400
        interpolator = bezier
    }
    )

private fun createPasswordToTokenTransition(): Transition = TransitionSet()
    .addTransition(
        Fade().apply {
        addTarget(R.id.view_login_pw_layout)
        interpolator = bezier
        duration = 400
    }
    )
    .addTransition(
        Fade().apply {
        addTarget(R.id.view_login_token_layout)
        interpolator = bezier
        startDelay = 400
        duration = 400
    }
    )

private fun createEmailToTokenTransition(): Transition = TransitionSet()
    .addTransition(
        Fade().apply {
        addTarget(R.id.view_login_email_layout)
        duration = 400
        interpolator = bezier
    }
    )
    .addTransition(
        transformEmail(500).apply {
        startDelay = 250
    }
    )
    .addTransition(
        Fade().apply {
        addTarget(R.id.view_login_token_layout)
        interpolator = bezier
        startDelay = 400
        duration = 400
    }
    )
    .addTransition(
        buttonChanger(R.id.btn_create_account, R.id.btn_where_is)
    )

private fun createTokenToEmailTransition(): Transition = TransitionSet()
    .addTransition(
        transformEmail(500).apply {
            startDelay = 50
        }
    )
    .addTransition(
        fadeOutSlide(listOf(R.id.view_login_token_layout)).apply {
            duration = 400
        }
    )
    .addTransition(
        Fade().apply {
        addTarget(R.id.view_login_email_layout)
        duration = 400
        startDelay = 400
        interpolator = bezier
    }
    )
    .addTransition(
        buttonChanger(R.id.btn_where_is, R.id.btn_create_account)
    )

private fun transformEmail(duration: Long = DEFAULT_DURATION) =
    transform(duration).also { it.addTarget("email") }

private fun transform(duration: Long = DEFAULT_DURATION) =
    ChangeTransform().also {
        it.duration = duration
        it.interpolator = bezier
    }

private fun fadeInSlide(
    items: List<Int>? = null,
    slideKept: Float = 0.02f,
    duration: Long = DEFAULT_DURATION
) = TransitionSet().apply {
    items?.forEach {
        addTarget(it)
    }
    addTransition(
        Fade().apply {
        interpolator = bezier
    }
    )

    addTransition(
        Slide().apply {
        interpolator = KeepEndEaseInOutInterpolator(slideKept)
    }
    )

    this.duration = duration
}

private fun fadeOutSlide(
    itemIds: List<Int>? = null,
    slideKept: Float = 0.02f,
    duration: Long = DEFAULT_DURATION
) = TransitionSet().apply {
    itemIds?.forEach {
        addTarget(it)
    }
    addTransition(
        Slide().apply {
        interpolator = KeepStartEaseInOutInterpolator(slideKept)
    }
    )

    addTransition(
        Fade().apply {
        interpolator = bezier
    }
    )

    this.duration = duration
}

private fun buttonChanger(idOut: Int, idIn: Int) = TransitionSet().apply {
    addTransition(
        fadeOutSlide(listOf(idOut)).apply {
            startDelay = 50
        }
    )
    addTransition(
        fadeInSlide(listOf(idIn)).apply {
            startDelay = 400
        }
    )
}

class KeepEndEaseInOutInterpolator(val keep: Float) : TimeInterpolator {
    override fun getInterpolation(t: Float): Float {
        return 1f - keep + bezier.getInterpolation(t) * keep
    }
}

class KeepStartEaseInOutInterpolator(val keep: Float) : TimeInterpolator {
    override fun getInterpolation(t: Float): Float {
        return bezier.getInterpolation(t) * keep
    }
}

val bezier = PathInterpolatorCompat.create(0.64f, 0.04f, 0.35f, 1f)
