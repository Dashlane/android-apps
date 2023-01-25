package com.dashlane.authenticator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import com.dashlane.authenticator.AuthenticatorEnterActivationKey.Companion.RESULT_OTP_SECRET
import com.dashlane.authenticator.AuthenticatorResultIntro.Companion.EXTRA_SUCCESS
import com.dashlane.barcodescanner.BarCodeCaptureActivity
import com.dashlane.barcodescanner.BarCodeCaptureActivity.Companion.BARCODE_FORMAT
import com.dashlane.barcodescanner.BarCodeCaptureActivity.Companion.HEADER
import com.dashlane.barcodescanner.BarCodeCaptureActivity.Companion.RESULT_EXTRA_BARCODE_VALUES
import com.dashlane.help.HelpCenterLink
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.ui.util.DialogHelper
import com.dashlane.useractivity.hermes.TrackingLogUtils
import com.dashlane.util.isSemanticallyNull
import com.dashlane.util.launchUrl
import com.dashlane.util.setCurrentPageView
import com.google.mlkit.vision.barcode.common.Barcode
import com.skocken.presentation.presenter.BasePresenter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject




private const val DASHLANE_DOMAIN = "dashlane"

@AndroidEntryPoint
class AuthenticatorIntro : DashlaneActivity() {

    private lateinit var presenter: IntroScreenContract.Presenter

    @Inject
    lateinit var logger: AuthenticatorLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_intro)
        val name = intent.extras?.getString(EXTRA_CREDENTIAL_NAME)
        val itemId = intent.extras?.getString(EXTRA_CREDENTIAL_ID)
        val topDomain = intent.extras?.getString(EXTRA_CREDENTIAL_TOP_DOMAIN)
        val packageName = intent.extras?.getString(EXTRA_CREDENTIAL_PACKAGE_NAME)
        val professional = intent.extras?.getBoolean(EXTRA_CREDENTIAL_PROFESSIONAL) ?: false
        val successIntent = Intent(this, AuthenticatorResultIntro::class.java).apply {
            putExtra(EXTRA_SUCCESS, true)
        }
        val askRetry = registerForActivityResult(InvalidOtp(name)) { wantsRetry ->
            if (!wantsRetry) {
                setResult(RESULT_CANCELED, Intent().apply {
                    putExtra(RESULT_ITEM_ID, itemId)
                })
                finish()
            }
        }
        val scanQrCode = registerForActivityResult(ScanQrCode(name)) {
            notifyPotentialOtp(name, it, itemId, successIntent, true, askRetry)
        }
        
        
        val enterKey = if (itemId == null || name == null) {
            null
        } else {
            registerForActivityResult(EnterActivationKey(name)) {
                notifyPotentialOtp(name, it, itemId, successIntent, false, askRetry)
            }
        }
        val domain = if (topDomain != null || packageName != null) {
            TrackingLogUtils.createDomainForLog(topDomain, packageName)
        } else {
            null
        }
        presenter = Presenter(
            itemId,
            name,
            domain,
            professional,
            scanQrCode,
            enterKey,
            logger
        )
        presenter.setView(IntroScreenViewProxy(this))
        setCurrentPageView(AnyPage.TOOLS_AUTHENTICATOR_SETUP)
    }

    private fun notifyPotentialOtp(
        credentialName: String?,
        result: Result,
        itemId: String?,
        successIntent: Intent,
        byScan: Boolean,
        askRetry: ActivityResultLauncher<Unit?>
    ) {
        if (result.activityResult != Activity.RESULT_OK) return
        when (val otp = result.otp) {
            is Totp, is Hotp -> {
                if (credentialName == null && otp.issuer.isSemanticallyNull()) {
                    
                    logger.logMissingLoginError(otp)
                    askRetry.launch(null)
                    return
                }
                
                if (otp.getPin() == null) {
                    logger.logPinError(itemId, otp)
                    askRetry.launch(null)
                    return
                }
                if (otp.issuer?.contains(DASHLANE_DOMAIN, true) == true) {
                    showDashlaneDomainDialog()
                    return
                }
                setResult(RESULT_OK, Intent().apply {
                    putExtra(RESULT_ITEM_ID, itemId)
                    if (otp is Totp) {
                        putExtra(RESULT_OTP, otp)
                    } else if (otp is Hotp) {
                        putExtra(RESULT_OTP, otp)
                    }
                })
                
                itemId?.let {
                    logger.logCompleteAdd2fa(it, otp)
                    startActivity(successIntent.apply {
                        putExtra(EXTRA_CREDENTIAL_NAME, credentialName ?: otp.issuer)
                    })
                }
                
                
                finish()
            }
            
            else -> {
                logger.logUnknownError(itemId, otp, byScan)
                askRetry.launch(null)
            }
        }
    }

    private fun showDashlaneDomainDialog() {
        DialogHelper()
            .builder(this)
            .setMessage(getString(R.string.authenticator_error_dashlane_domain_message))
            .setCancelable(true)
            .setPositiveButton(R.string.authenticator_error_dashlane_domain_button) { _, _ -> }
            .show()
    }

    private class Presenter(
        private val itemId: String?,
        private val credentialName: String?,
        domain: Domain?,
        professional: Boolean,
        private val scanQrCode: ActivityResultLauncher<Unit?>,
        private val enterKey: ActivityResultLauncher<Unit?>?,
        private val logger: AuthenticatorLogger
    ) : BasePresenter<IntroScreenContract.DataProvider, IntroScreenContract.ViewProxy>(),
        IntroScreenContract.Presenter {

        init {
            logger.setup(professional, domain)
        }

        override fun onViewChanged() {
            super.onViewChanged()
            view.apply {
                setImageResource(R.drawable.ic_authenticator)
                val title = if (credentialName != null) {
                    resources.getString(R.string.authenticator_intro_title, credentialName)
                } else {
                    resources.getString(R.string.authenticator_intro_title_unknown)
                }
                setTitle(title)
                setDescription(R.string.authenticator_intro_body)
                setPositiveButton(R.string.authenticator_intro_positive_button)
                credentialName?.also {
                    setNegativeButton(R.string.authenticator_intro_neutral_button)
                }
                setLinks(R.string.authenticator_intro_learn_more_link)
            }
        }

        override fun onClickPositiveButton() {
            scanQrCode.launch(null)
            logger.logStartAdd2fa(itemId, true)
            setCurrentPageView(AnyPage.TOOLS_AUTHENTICATOR_SETUP_QR_CODE)
        }

        override fun onClickNegativeButton() {
            enterKey!!.launch(null)
            logger.logStartAdd2fa(itemId!!, false)
            setCurrentPageView(AnyPage.TOOLS_AUTHENTICATOR_SETUP_TEXT_CODE)
        }

        override fun onClickNeutralButton() {
            
        }

        override fun onClickLink(position: Int, label: Int) {
            context?.launchUrl(HelpCenterLink.ARTICLE_AUTHENTICATOR.uri)
        }
    }

    class ScanQrCode(private val credentialName: String?) :
        ActivityResultContract<Unit?, Result>() {
        override fun createIntent(context: Context, input: Unit?) =
            Intent(context, BarCodeCaptureActivity::class.java).apply {
                credentialName?.let {
                    putExtra(
                        HEADER,
                        context.getString(R.string.authenticator_qr_code_scan_header, it)
                    )
                } ?: putExtra(
                    HEADER,
                    context.getString(R.string.authenticator_qr_code_scan_header_unknown)
                )

                putExtra(BARCODE_FORMAT, Barcode.FORMAT_QR_CODE)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Result {
            if (resultCode != Activity.RESULT_OK || intent == null) return Result(resultCode)
            val barcode = intent.getStringArrayExtra(RESULT_EXTRA_BARCODE_VALUES)
            if (barcode == null || barcode.isEmpty()) return Result(resultCode)
            
            val uri = Uri.parse(barcode[0])
            return Result(resultCode, UriParser.parse(uri))
        }
    }

    class EnterActivationKey(
        private val credentialName: String
    ) : ActivityResultContract<Unit?, Result>() {
        override fun createIntent(context: Context, input: Unit?) =
            Intent(context, AuthenticatorEnterActivationKey::class.java).apply {
                putExtra(EXTRA_CREDENTIAL_NAME, credentialName)
            }

        override fun parseResult(resultCode: Int, intent: Intent?): Result {
            if (resultCode != Activity.RESULT_OK || intent == null) return Result(resultCode)
            
            
            val secret = intent.getStringExtra(RESULT_OTP_SECRET)?.sanitizeOtpSecret()
            if (secret == null || secret.isEmpty()) return Result(resultCode)
            return Result(resultCode, Totp(secret = secret))
        }
    }

    class InvalidOtp(private val credentialName: String?) :
        ActivityResultContract<Unit?, Boolean>() {
        override fun createIntent(context: Context, input: Unit?) =
            Intent(context, AuthenticatorResultIntro::class.java).apply {
                credentialName?.let { putExtra(EXTRA_CREDENTIAL_NAME, it) }
                putExtra(EXTRA_SUCCESS, false)
            }

        override fun parseResult(resultCode: Int, intent: Intent?) = resultCode == Activity.RESULT_OK
    }

    data class Result(val activityResult: Int, val otp: Otp? = null)

    companion object {
        const val EXTRA_CREDENTIAL_ID = "credentialId"
        const val EXTRA_CREDENTIAL_NAME = "credentialName"
        const val EXTRA_CREDENTIAL_TOP_DOMAIN = "credentialTopDomain"
        const val EXTRA_CREDENTIAL_PACKAGE_NAME = "credentialPackageName"
        const val EXTRA_CREDENTIAL_PROFESSIONAL = "credentialProfessional"
        const val RESULT_OTP = "result_otp"
        const val RESULT_ITEM_ID = "result_item_id"
    }
}
