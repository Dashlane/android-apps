package com.dashlane.ui.credential.passwordgenerator

import android.app.Activity
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringDef
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.dashlane.R
import com.dashlane.hermes.LogRepository
import com.dashlane.passwordstrength.PasswordStrength
import com.dashlane.session.SessionManager
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment
import com.dashlane.ui.fragments.PasswordGenerationCallback
import com.dashlane.ui.fragments.PasswordGeneratorFragment
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.ObsoleteCoroutinesApi

@OptIn(ObsoleteCoroutinesApi::class)
@AndroidEntryPoint
class PasswordGeneratorDialog : NotificationDialogFragment(), PasswordGenerationCallback {

    @Inject
    lateinit var logRepository: LogRepository

    @Inject
    lateinit var sessionManager: SessionManager

    @Origin
    lateinit var origin: String
    private lateinit var domainAsking: String

    private val passwordGeneratorFragment: PasswordGeneratorFragment
        get() = requireActivity().supportFragmentManager
            .findFragmentById(R.id.dialogPasswordGeneratorFragment) as PasswordGeneratorFragment

    override fun onPasswordGenerated() {
        
        setButtonEnable(DialogInterface.BUTTON_POSITIVE, true)
    }

    override fun passwordSaved(
        generatedPassword: VaultItem<SyncObject.GeneratedPassword>,
        strength: PasswordStrength?
    ) {
        setFragmentResult(
            PASSWORD_GENERATOR_REQUEST_KEY,
            bundleOf(
                PASSWORD_GENERATOR_RESULT_ID to generatedPassword.syncObject.id,
                PASSWORD_GENERATOR_RESULT_PASSWORD to generatedPassword.syncObject.password.toString()
            )
        )
    }

    override fun restoreDominantColor(color: Int) {
        
    }

    override fun showPreviouslyGenerated() {
        
    }

    override fun onCreateDialogCustomView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater?.inflate(R.layout.fragment_dialog_password_generator, container, false)
        view?.apply {
            val fragmentViewGroup = findViewById<ViewGroup>(R.id.dialogPasswordGeneratorFragment)
            val copyButton = fragmentViewGroup.findViewById<View>(R.id.copy_generated_password)
            copyButton.visibility = View.GONE
        }
        passwordGeneratorFragment.log75Subtype = origin
        passwordGeneratorFragment.passwordGenerationCallback = this
        return view
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        origin = requireArguments().getString(PASSWORD_GENERATOR_ARG_ORIGIN)!!
        domainAsking = requireArguments().getString(PASSWORD_GENERATOR_ARG_DOMAIN)!!

        
        setButtonEnable(DialogInterface.BUTTON_POSITIVE, false)
    }

    override fun onClickPositiveButton() {
        super.onClickPositiveButton()
        passwordGeneratorFragment.copyAndSaveGeneratedPassword(domainAsking, false)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().supportFragmentManager.beginTransaction().remove(passwordGeneratorFragment)
            .commitNowAllowingStateLoss()
    }

    @StringDef(EDIT_VIEW, CREATION_VIEW)
    @Retention(AnnotationRetention.SOURCE)
    annotation class Origin

    companion object {
        const val DIALOG_PASSWORD_GENERATOR_TAG = "PASSWORD_GENERATOR_POPUP"
        const val PASSWORD_GENERATOR_ARG_ORIGIN = "password_generator_arg_origin"
        const val PASSWORD_GENERATOR_ARG_DOMAIN = "password_generator_arg_domain"
        const val PASSWORD_GENERATOR_REQUEST_KEY = "password_generator_request_key"
        const val PASSWORD_GENERATOR_RESULT_ID = "password_generator_result_id"
        const val PASSWORD_GENERATOR_RESULT_PASSWORD = "password_generator_result_password"
        const val EDIT_VIEW = "editPasswordView"
        const val CREATION_VIEW = "addPasswordView"

        fun newInstance(
            activity: Activity,
            @Origin origin: String,
            domainAsking: String
        ): PasswordGeneratorDialog {
            return Builder()
                .setArgs(
                    bundleOf(
                        PASSWORD_GENERATOR_ARG_ORIGIN to origin,
                        PASSWORD_GENERATOR_ARG_DOMAIN to domainAsking
                    )
                )
                .setPositiveButtonText(activity, R.string.use)
                .setNegativeButtonText(activity, R.string.cancel)
                .setClickNegativeOnCancel(true)
                .build(PasswordGeneratorDialog())
        }
    }
}