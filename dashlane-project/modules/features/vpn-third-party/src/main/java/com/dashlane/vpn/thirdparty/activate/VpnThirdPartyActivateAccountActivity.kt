package com.dashlane.vpn.thirdparty.activate

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.dashlane.util.CurrentPageViewLogger
import com.dashlane.vpn.thirdparty.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VpnThirdPartyActivateAccountActivity : AppCompatActivity(), CurrentPageViewLogger.Owner,
    VpnThirdPartyActivateAccountErrorListener, VpnThirdPartySetupEmailFragment.Listener {

    override val currentPageViewLogger by lazy { CurrentPageViewLogger(this) }
    private var presenter: VpnThirdPartyActivateAccountPresenter? = null
    private var errorType = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vpn_third_party_activate_account)
    }

    override fun onPresenterReady(presenter: VpnThirdPartyActivateAccountPresenter) {
        this.presenter = presenter
        errorType = -1
    }

    override fun onError(errorType: Int) {
        this.errorType = errorType
    }

    override fun onContactSupport() {
        if (errorType == VpnThirdPartyActivateAccountErrorListener.ERROR_TYPE_ACCOUNT_EXISTS) {
            presenter?.onContactProviderSupport()
        } else {
            presenter?.onContactSupport()
        }
    }

    override fun onTryAgain() {
        if (errorType == VpnThirdPartyActivateAccountErrorListener.ERROR_TYPE_ACCOUNT_EXISTS) {
            presenter?.onTryAgainAccountExists()
        } else {
            lifecycleScope.launch { presenter?.onTryAgain() }
        }
    }
}