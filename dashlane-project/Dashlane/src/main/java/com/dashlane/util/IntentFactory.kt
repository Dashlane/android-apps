package com.dashlane.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.dashlane.R



object IntentFactory {

    @JvmStatic
    fun sendShareWithFriendsIntent(activity: Context, refid: String?) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        @Suppress("DEPRECATION")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        
        intent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.checkout_dashlane))
        intent.putExtra(
            Intent.EXTRA_TEXT, String.format(
                activity.getString(R.string.refferal_text), TextUtil
                    .generateRefferalUrl(refid)
            )
        )
        if (intent.resolveActivity(activity.packageManager) == null) {
            ToasterImpl(activity).show(
                activity.getString(R.string.contact_system_administrator),
                Toast.LENGTH_LONG
            )
        } else {
            val chooser = Intent.createChooser(intent, activity.getString(R.string.invites))
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            DevUtil.startActivityOrDefaultErrorMessage(activity, chooser)
        }
    }

    @JvmStatic
    fun sendMarketIntent(context: Context) {
        val playStore =
            Uri.parse("https://play.google.com/store/apps/details?id=com.dashlane")
        DevUtil.startActivityOrDefaultErrorMessage(
            context,
            Intent(Intent.ACTION_VIEW, playStore)
        )
    }
}