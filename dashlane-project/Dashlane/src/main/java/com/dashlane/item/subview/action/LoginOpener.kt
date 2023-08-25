package com.dashlane.item.subview.action

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.dashlane.R
import com.dashlane.dagger.singleton.KnownApplicationEntryPoint
import com.dashlane.ext.application.ExternalApplication
import com.dashlane.ui.activities.fragments.list.action.ActionItemHelper
import com.dashlane.util.ToasterImpl
import com.dashlane.util.tryOrNull
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.EntryPointAccessors

@SuppressLint("InflateParams")
class LoginOpener(private val activity: Activity) {
    private val layout: LinearLayout by lazy {
        LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_dialog, null) as LinearLayout
    }
    private val dialog: BottomSheetDialog by lazy { BottomSheetDialog(activity) }

    fun show(url: String?, packageNames: Set<String>, listener: Listener?) {
        
        val defaultIntent = tryOrNull { Uri.parse(url) }?.let { uri ->
            Intent(Intent.ACTION_VIEW).apply { data = uri }
        }
        val defaultApplicationFromUrl = defaultIntent?.let { ExternalApplication.of(activity, defaultIntent) }
        val defaultApplicationPackageName = defaultApplicationFromUrl?.packageName

        
        val applicationOptions = if (url == null) {
            emptyList()
        } else {
            getApplicationOptions(url, packageNames, defaultApplicationPackageName)
        }

        
        if (defaultApplicationPackageName != null && applicationOptions.isEmpty()) {
            onOpen(defaultIntent, defaultApplicationPackageName, listener)
            return
        }
        if (defaultApplicationPackageName == null && applicationOptions.size == 1) {
            val packageName = applicationOptions[0].packageName
            activity.packageManager.getLaunchIntentForPackage(packageName)?.let {
                onOpen(it, packageName, listener)
                return
            }
        }

        
        if (defaultApplicationPackageName == null && applicationOptions.isEmpty()) {
            ToasterImpl(activity).show(
                text = activity.getString(com.dashlane.ui.R.string.warning_no_browser_enabled_on_device),
                duration = Toast.LENGTH_LONG
            )
            return
        }

        
        buildApplicationsBottomSheet(applicationOptions, defaultApplicationFromUrl, defaultIntent, listener)
    }

    private fun getApplicationOptions(
        url: String,
        packageNames: Set<String>,
        defaultPackageName: String?
    ): List<ExternalApplication> {
        val knownApplicationProvider = EntryPointAccessors.fromApplication(
            activity,
            KnownApplicationEntryPoint::class.java
        ).knownApplicationProvider
        return packageNames.plus(knownApplicationProvider.getPackageNamesCanOpen(url))
            .let {
                
                defaultPackageName?.let { packageNames.minus(defaultPackageName) } ?: it
            }
            .mapNotNull { ExternalApplication.of(activity, it) }
            .sortedBy { it.title }
    }

    private fun buildApplicationsBottomSheet(
        applicationOptions: List<ExternalApplication>,
        defaultApplicationFromUrl: ExternalApplication?,
        defaultIntent: Intent?,
        listener: Listener?
    ) {
        
        dialog.setContentView(layout)
        
        (layout.parent as View).background = null
        buildApplicationOptions(applicationOptions, defaultApplicationFromUrl, defaultIntent, listener)
        dialog.show()

        val sheetInternal: View = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)!!
        BottomSheetBehavior.from(sheetInternal).state = BottomSheetBehavior.STATE_EXPANDED

        listener?.onShowOption()
    }

    private fun buildApplicationOptions(
        options: List<ExternalApplication>,
        defaultApplication: ExternalApplication?,
        defaultIntent: Intent?,
        listener: Listener?
    ) {
        layout.removeAllViews()
        
        if (defaultApplication != null && defaultIntent != null) {
            defaultApplication.addLayoutAction(activity, layout) {
                onOpen(defaultIntent, defaultApplication.packageName, listener)
                dialog.dismiss()
            }
        }
        
        options.forEach { applicationEntry ->
            val packageName = applicationEntry.packageName
            val intent = activity.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                applicationEntry.addLayoutAction(activity, layout) {
                    onOpen(intent, packageName, listener)
                    dialog.dismiss()
                }
            }
        }
    }

    private fun onOpen(intent: Intent, packageName: String, listener: Listener?) {
        listener?.onLogin(packageName)
        activity.startActivity(intent)
    }

    private fun ExternalApplication.addLayoutAction(
        activity: Activity,
        layout: LinearLayout,
        action: () -> Unit
    ) = layout.addView(ActionItemHelper().createNewActionItem(activity, drawable, false, title, null, action))

    interface Listener {
        fun onShowOption()
        fun onLogin(packageName: String)
    }
}