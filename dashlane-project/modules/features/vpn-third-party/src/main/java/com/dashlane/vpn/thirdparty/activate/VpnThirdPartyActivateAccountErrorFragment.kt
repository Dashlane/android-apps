package com.dashlane.vpn.thirdparty.activate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.dashlane.vpn.thirdparty.R
import com.dashlane.vpn.thirdparty.activate.VpnThirdPartyActivateAccountErrorListener.Companion.ERROR_TYPE_UNKNOWN



class VpnThirdPartyActivateAccountErrorFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val listener = activity as? VpnThirdPartyActivateAccountErrorListener
        listener?.onError(ERROR_TYPE_UNKNOWN)
        return inflater.inflate(
            R.layout.fragment_vpn_third_party_activate_account_error,
            container,
            false
        ).also {
            it.findViewById<Button>(R.id.vpn_activation_error_negative_button)
                .setOnClickListener {
                    listener?.onContactSupport()
                }
            it.findViewById<Button>(R.id.vpn_activation_error_positive_button)
                .setOnClickListener {
                    listener?.onTryAgain()
                }
        }
    }
}