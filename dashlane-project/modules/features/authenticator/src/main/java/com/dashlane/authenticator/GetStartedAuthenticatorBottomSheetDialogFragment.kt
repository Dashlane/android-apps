package com.dashlane.authenticator

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.TextDelegate
import com.dashlane.ui.ExpandedBottomSheetDialogFragment

class GetStartedAuthenticatorBottomSheetDialogFragment : ExpandedBottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_authenticator_get_started, container, false)
        .apply {
            findViewById<LottieAnimationView>(R.id.authenticator_get_started_logo).apply {
                val textDelegate = TextDelegate(this)
                setTextDelegate(textDelegate)
                textDelegate.setText(
                    "%CONTINUE%",
                    getString(R.string.authenticator_get_started_animation_continue)
                )
                textDelegate.setText(
                    "%LOG IN%",
                    getString(R.string.authenticator_get_started_animation_login)
                )
                setFontAssetDelegate(object : FontAssetDelegate() {
                    override fun fetchFont(fontFamily: String?): Typeface {
                        val fontId =
                            resources.getIdentifier(fontFamily, "font", context.packageName)
                        return ResourcesCompat.getFont(context, fontId)!!
                    }
                })
            }
            findViewById<Button>(R.id.authenticator_get_started_button)!!.setOnClickListener {
                dismiss()
            }
        }
}