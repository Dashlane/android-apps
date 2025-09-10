package com.dashlane.authenticator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import com.dashlane.authenticator.AuthenticatorEnterActivationKey.Companion.RESULT_OTP_SECRET
import com.dashlane.authenticator.ui.AuthenticatorBoardingScreen
import com.dashlane.barcodescanner.BarCodeCaptureActivity
import com.dashlane.barcodescanner.BarCodeCaptureActivity.Companion.BARCODE_FORMAT
import com.dashlane.barcodescanner.BarCodeCaptureActivity.Companion.HEADER
import com.dashlane.barcodescanner.BarCodeCaptureActivity.Companion.RESULT_EXTRA_BARCODE_VALUES
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.help.HelpCenterLink
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.util.DialogHelper
import com.dashlane.useractivity.hermes.TrackingLogUtils
import com.dashlane.util.isSemanticallyNull
import com.dashlane.util.launchUrl
import com.dashlane.util.setCurrentPageView
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


private const val DASHLANE_DOMAIN = "dashlane"

@AndroidEntryPoint
class AuthenticatorBoardingActivity : DashlaneActivity() {
    @Inject
    lateinit var logger: AuthenticatorLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val credentialName = intent.extras?.getString(EXTRA_CREDENTIAL_NAME)
        val itemId = intent.extras?.getString(EXTRA_CREDENTIAL_ID)
        val topDomain = intent.extras?.getString(EXTRA_CREDENTIAL_TOP_DOMAIN)
        val packageName = intent.extras?.getString(EXTRA_CREDENTIAL_PACKAGE_NAME)
        val professional = intent.extras?.getBoolean(EXTRA_CREDENTIAL_PROFESSIONAL) ?: false

        val askRetry = registerForActivityResult(InvalidOtp(credentialName)) { wantsRetry ->
            if (!wantsRetry) {
                setResult(RESULT_CANCELED, Intent().apply { putExtra(RESULT_ITEM_ID, itemId) })
                finish()
            }
        }
        val scanQrCode = registerForActivityResult(ScanQrCode(credentialName)) {
            notifyPotentialOtp(credentialName, it, itemId, true, askRetry)
        }
        
        
        val enterKey = if (itemId == null || credentialName == null) {
            null
        } else {
            registerForActivityResult(EnterActivationKey(credentialName)) {
                notifyPotentialOtp(credentialName, it, itemId, false, askRetry)
            }
        }
        val domain = if (topDomain != null || packageName != null) {
            TrackingLogUtils.createDomainForLog(topDomain, packageName)
        } else {
            null
        }
        if (savedInstanceState == null) {
            logger.setup(professional, domain)
        }
        setContent {
            DashlaneTheme {
                AuthenticatorBoardingScreen(
                    credentialName = credentialName,
                    onScanQrCodeClick = {
                        scanQrCode.launch(null)
                        logger.logStartAdd2fa(itemId, true)
                        setCurrentPageView(AnyPage.TOOLS_AUTHENTICATOR_SETUP_QR_CODE)
                    },
                    onEnterSetupCodeClick = {
                        enterKey!!.launch(null)
                        logger.logStartAdd2fa(itemId!!, false)
                        setCurrentPageView(AnyPage.TOOLS_AUTHENTICATOR_SETUP_TEXT_CODE)
                    },
                    onLinkClick = ::onLinkClick,
                )
            }
        }
        setCurrentPageView(AnyPage.TOOLS_AUTHENTICATOR_SETUP)
    }

    private fun notifyPotentialOtp(
        credentialName: String?,
        result: Result,
        itemId: String?,
        byScan: Boolean,
        askRetry: ActivityResultLauncher<Unit?>,
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
                setResult(
                    RESULT_OK,
                    Intent().apply {
                        putExtra(RESULT_ITEM_ID, itemId)
                        if (otp is Totp) {
                            putExtra(RESULT_OTP, otp)
                        } else if (otp is Hotp) {
                            putExtra(RESULT_OTP, otp)
                        }
                    },
                )
                
                itemId?.let {
                    logger.logCompleteAdd2fa(it, otp)
                    val successIntent = AuthenticatorResultActivity.newSuccessIntent(
                        context = this,
                        credentialName = credentialName ?: otp.issuer,
                    )
                    startActivity(successIntent)
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

    private fun onLinkClick() {
        launchUrl(HelpCenterLink.ARTICLE_AUTHENTICATOR.androidUri)
    }

    class ScanQrCode(private val credentialName: String?) :
        ActivityResultContract<Unit?, Result>() {
        override fun createIntent(context: Context, input: Unit?) =
            Intent(context, BarCodeCaptureActivity::class.java).apply {
                credentialName?.let {
                    putExtra(
                        HEADER,
                        context.getString(R.string.authenticator_qr_code_scan_header, it),
                    )
                } ?: putExtra(
                    HEADER,
                    context.getString(R.string.authenticator_qr_code_scan_header_unknown),
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
        private val credentialName: String,
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
            AuthenticatorResultActivity.newFailedIntent(
                context = context,
                credentialName = credentialName,
            )

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