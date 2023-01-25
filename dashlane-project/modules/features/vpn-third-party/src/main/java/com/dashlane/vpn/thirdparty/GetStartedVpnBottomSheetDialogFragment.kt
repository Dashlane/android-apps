package com.dashlane.vpn.thirdparty

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.net.toUri
import com.dashlane.ui.ExpandedBottomSheetDialogFragment
import com.dashlane.util.setTextWithLinks

class GetStartedVpnBottomSheetDialogFragment : ExpandedBottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_vpn_third_party_get_started, container, false)
        .apply {
            findViewById<TextView>(R.id.vpn_third_party_get_started_text).setTextWithLinks(
                R.string.vpn_third_party_get_started_dialog_body,
                listOf(R.string.vpn_third_party_get_started_dialog_body_link to "https://www.hotspotshield.com/".toUri())
            )
            findViewById<Button>(R.id.vpn_third_party_get_started_button)!!.setOnClickListener {
                dismiss()
            }
        }
}