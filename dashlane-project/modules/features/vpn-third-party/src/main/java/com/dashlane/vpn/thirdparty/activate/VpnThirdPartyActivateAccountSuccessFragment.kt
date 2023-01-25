package com.dashlane.vpn.thirdparty.activate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dashlane.vpn.thirdparty.R



class VpnThirdPartyActivateAccountSuccessFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_vpn_third_party_activate_account_success, container, false)
    }
}