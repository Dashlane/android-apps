package com.dashlane.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import com.dashlane.ui.R
import com.google.android.material.snackbar.Snackbar

@Suppress("WrongConstant")
object SnackbarUtils {
    fun showSnackbar(
        anchorView: View,
        text: CharSequence,
        duration: Int = Snackbar.LENGTH_LONG,
        block: (Snackbar.() -> Unit)? = null
    ): Snackbar = Snackbar.make(anchorView, text, duration).apply {
        
        view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).run {
            maxLines = Int.MAX_VALUE
        }
        setTextColor(context.getColor(R.color.text_neutral_standard))
        setActionTextColor(context.getColor(R.color.text_neutral_standard))
        val backgroundColor = ColorUtils.compositeColors(
            context.getColor(R.color.container_expressive_neutral_quiet_idle),
            context.getColor(R.color.container_agnostic_neutral_quiet)
        )
        setBackgroundTint(backgroundColor)
        block?.invoke(this)
        show()
    }

    fun showSnackbar(
        activity: Activity,
        text: CharSequence,
        duration: Int = Snackbar.LENGTH_LONG,
        block: (Snackbar.() -> Unit)? = null
    ): Snackbar = showSnackbar(
        activity.findContentParent(),
        text,
        duration,
        block
    )

    @JvmStatic
    fun showPermissionSnackbar(
        anchorView: View,
        text: CharSequence,
        duration: Int = Snackbar.LENGTH_LONG,
        onGoToSettings: View.OnClickListener? = null
    ): Snackbar = showSnackbar(
        anchorView,
        text,
        duration
    ) {
        setAction(R.string.settings) {
            onGoToSettings?.onClick(it)
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:${context.packageName}"))

            context.startActivity(intent)
        }
    }

    @JvmStatic
    fun showPermissionSnackbar(
        activity: Activity,
        text: CharSequence,
        duration: Int = Snackbar.LENGTH_LONG,
        onGoToSettings: View.OnClickListener? = null
    ) = showPermissionSnackbar(
        activity.findContentParent(),
        text,
        duration,
        onGoToSettings
    )

    @JvmStatic
    fun getContactPermissionText(context: Context): String =
        context.getString(R.string.allow_dashlane_to_access_your_contacts)

    @JvmStatic
    fun getStoragePermissionText(context: Context): String =
        context.getString(R.string.download_file_permission_required)
}