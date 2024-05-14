package com.dashlane.login.sso.migration

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.dashlane.R
import com.dashlane.help.HelpCenterLink
import com.dashlane.help.newIntent
import com.dashlane.masterpassword.ChangeMasterPasswordLogoutHelper
import com.dashlane.session.SessionManager
import com.dashlane.session.SessionRestorer
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.util.safelyStartBrowserActivity
import com.skocken.presentation.presenter.BasePresenter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MigrationToSsoMemberIntroActivity : DashlaneActivity() {

    @Inject
    lateinit var changeMasterPasswordLogoutHelper: ChangeMasterPasswordLogoutHelper

    @Inject
    lateinit var sessionRestorer: SessionRestorer

    @Inject
    lateinit var sessionManager: SessionManager

    override var requireUserUnlock = false

    private lateinit var presenter: Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val migrationToSsoMemberIntent = intent.getParcelableExtraCompat<Intent>(EXTRA_MIGRATION_TO_SSO_MEMBER_INTENT)

        if (migrationToSsoMemberIntent == null) {
            finish()
            return
        }

        setContentView(R.layout.activity_intro)
        val viewProxy = IntroScreenViewProxy(this)
        presenter = Presenter(
            logoutHelper = changeMasterPasswordLogoutHelper,
            migrationToSsoMemberIntent = migrationToSsoMemberIntent,
            sessionRestorer = sessionRestorer
        ).apply { setView(viewProxy) }
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        presenter.onBackPressed()
    }

    private class Presenter(
        private val logoutHelper: ChangeMasterPasswordLogoutHelper,
        private val migrationToSsoMemberIntent: Intent,
        sessionRestorer: SessionRestorer
    ) : BasePresenter<IntroScreenContract.DataProvider, IntroScreenContract.ViewProxy>(),
        IntroScreenContract.Presenter {

        init {
            sessionRestorer.restoredSessionMigrationToSsoMemberInfo = null
        }

        override fun onViewChanged() {
            view.run {
                setImageResource(R.drawable.ic_migration_to_sso_member_intro)
                setTitle(R.string.sso_member_migration_intro_title)
                setDescription(R.string.sso_member_migration_intro_description)
                setLinks(R.string.sso_member_migration_intro_link)
                setInfobox(R.string.sso_member_migration_intro_info)
                setPositiveButton(R.string.sso_member_migration_intro_cta_positive)
                setNegativeButton(R.string.sso_member_migration_intro_cta_negative)
            }
        }

        override fun onClickPositiveButton() {
            activity?.startActivity(migrationToSsoMemberIntent)
        }

        override fun onClickNegativeButton() {
            logout()
        }

        override fun onClickNeutralButton() = Unit

        override fun onClickLink(position: Int, label: Int) {
            activity?.run {
                safelyStartBrowserActivity(
                    HelpCenterLink.ARTICLE_SSO_LOGIN.newIntent(
                        context = this
                    )
                )
            }
        }

        fun onBackPressed() {
            logout()
        }

        private fun logout() {
            activity?.run { logoutHelper.logout(this) }
        }
    }

    companion object {
        private const val EXTRA_MIGRATION_TO_SSO_MEMBER_INTENT = "migration_to_sso_member_intent"

        fun newIntent(
            context: Context,
            migrationToSsoMemberIntent: Intent
        ): Intent = Intent(context, MigrationToSsoMemberIntroActivity::class.java).putExtra(
            EXTRA_MIGRATION_TO_SSO_MEMBER_INTENT,
            migrationToSsoMemberIntent
        )
    }
}