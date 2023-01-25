package com.dashlane.ui.dialogs.fragments

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.util.isNullOrEmpty

open class NotificationDialogFragment : AbstractDialogFragment() {

    private val dialogHelper = SingletonProvider.getDialogHelper()
    private val announcementCenter = SingletonProvider.getAnnouncementCenter()

    var clicker: TwoButtonClicker? = null
    var title: String? = null
    var body: String? = null
    var positiveText: String? = null
    var negativeText: String? = null
    private var useNativeTile: Boolean = true

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = dialogHelper.builder(requireContext())
        title = arguments?.getString(ARG_TITLE)
        body = arguments?.getString(ARG_MESSAGE)
        positiveText = arguments?.getString(ARG_POSITIVE_BUTTON_TEXT)
        negativeText = arguments?.getString(ARG_NEGATIVE_BUTTON_TEXT)
        val customView = onCreateDialogCustomView(LayoutInflater.from(context), null, savedInstanceState)

        
        if (customView == null) {
            val customViewResId = arguments?.getInt(ARG_CUSTOM_VIEW_RES_ID) ?: 0
            if (customViewResId == 0) {
                builder.setTitleIfNeeded(title, useNativeTile)
                builder.setMessage(body)
            } else {
                if (useDialogTitle(customViewResId)) {
                    builder.setTitleIfNeeded(title, useNativeTile)
                }
                builder.setView(customViewResId)
            }
        } else {
            builder.setTitleIfNeeded(title, useNativeTile)
            builder.setView(customView)
        }

        
        builder.setPositiveButton(positiveText) { _, _ ->
            onClickPositiveButton()
            dismiss()
        }
        builder.setNegativeButton(negativeText) { _, _ ->
            onClickNegativeButton()
            dismiss()
        }
        builder.setCancelable(arguments?.getBoolean(ARG_CANCELABLE, true) ?: true)
        onPreCreateDialog(builder)

        val dialog = builder.create().apply {
            setOnShowListener { onDialogShown(this) }
        }

        return dialog
    }

    

    private fun useDialogTitle(customViewResId: Int): Boolean = when (customViewResId) {
        R.layout.include_dialog_image_text -> false
        else -> true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        fillCustomDialog()
    }

    override fun onResume() {
        super.onResume()
        
        announcementCenter.disable()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        if (arguments?.getBoolean(ARG_CLICK_POSITIVE_ON_CANCEL) == true) {
            onClickPositiveButton()
        }
        if (arguments?.getBoolean(ARG_CLICK_NEGATIVE_ON_CANCEL) == true) {
            onClickNegativeButton()
        }
    }

    override fun onStop() {
        super.onStop()
        
        announcementCenter.restorePreviousStateIfDisabled()
    }

    

    fun setButtonEnable(buttonId: Int, enable: Boolean) {
        val dialog = dialog
        if (dialog is AlertDialog) {
            dialog.getButton(buttonId).isEnabled = enable
        }
    }

    private fun fillCustomDialog() {
        val dialog = dialog ?: return
        val iconImageView = dialog.findViewById<ImageView>(R.id.dialog_icon)
        val titleTextView = dialog.findViewById<TextView>(R.id.dialog_title)
        val descriptionTextView = dialog.findViewById<TextView>(R.id.dialog_text)
        if (iconImageView == null || titleTextView == null || descriptionTextView == null) {
            return
        }
        titleTextView.text = arguments?.getString(ARG_TITLE)
        descriptionTextView.text = arguments?.getString(ARG_MESSAGE)
        fillCustomDialogImage(iconImageView, arguments)
    }

    private fun fillCustomDialogImage(iconImageView: ImageView, args: Bundle?) {
        val imageResId = args!!.getInt(ARG_IMAGE_RES_ID)
        if (imageResId != 0) {
            iconImageView.setImageResource(imageResId)
            return
        }
        val url = args.getString(ARG_IMAGE_URL)
        if (isNullOrEmpty(url)) {
            return
        }
        Glide.with(iconImageView.context)
            .load(url)
            .fitCenter()
            .into(iconImageView)
    }

    protected open fun onPreCreateDialog(builder: AlertDialog.Builder) {
        
    }

    protected open fun onDialogShown(dialog: AlertDialog?) {
        
    }

    protected open fun onCreateDialogCustomView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return null 
    }

    private fun getButtonNegativeDeepLink(args: Bundle?): String? {
        return args?.getString(ARG_NEGATIVE_BUTTON_DEEP_LINK)
    }

    private fun getButtonPositiveDeepLink(args: Bundle?): String? {
        return args?.getString(ARG_POSITIVE_BUTTON_DEEP_LINK)
    }

    protected open fun onClickPositiveButton() {
        if (clicker == null) {
            goToDeepLinkFromArg(getButtonPositiveDeepLink(arguments))
        } else {
            clicker?.onPositiveButton()
        }
    }

    protected open fun onClickNegativeButton() {
        if (clicker == null) {
            goToDeepLinkFromArg(getButtonNegativeDeepLink(arguments))
        } else {
            clicker?.onNegativeButton()
        }
    }

    private fun goToDeepLinkFromArg(deepLink: String?) {
        if (isNullOrEmpty(deepLink)) {
            return
        }
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(deepLink)
        SingletonProvider.getNavigator().handleDeepLink(intent)
    }

    interface TwoButtonClicker {
        fun onPositiveButton()
        fun onNegativeButton()
    }

    class Builder {

        private var clicker: TwoButtonClicker? = null
        private var args = Bundle()

        fun setArgs(bundle: Bundle): Builder {
            args = bundle
            return this
        }

        fun setTitle(context: Context, @StringRes stringRes: Int): Builder {
            return setTitle(context.getString(stringRes))
        }

        fun setTitle(title: String?): Builder {
            args.putString(ARG_TITLE, title)
            return this
        }

        fun setMessage(context: Context, @StringRes stringRes: Int): Builder {
            return setMessage(context.getString(stringRes))
        }

        fun setMessage(question: String?): Builder {
            args.putString(ARG_MESSAGE, question)
            return this
        }

        fun setCustomView(@LayoutRes layoutRes: Int): Builder {
            args.putInt(ARG_CUSTOM_VIEW_RES_ID, layoutRes)
            return this
        }

        fun setCustomViewWithBigIcon(@DrawableRes drawableResId: Int): Builder {
            args.putInt(ARG_IMAGE_RES_ID, drawableResId)
            return setCustomView(R.layout.include_dialog_image_text)
        }

        fun setNegativeButtonText(context: Context, @StringRes stringRes: Int): Builder {
            return setNegativeButtonText(context.getString(stringRes))
        }

        fun setNegativeButtonText(text: String?): Builder {
            args.putString(ARG_NEGATIVE_BUTTON_TEXT, text)
            return this
        }

        fun setNegativeButtonDeepLink(deepLink: String?): Builder {
            args.putString(ARG_NEGATIVE_BUTTON_DEEP_LINK, deepLink)
            return this
        }

        fun setPositiveButtonText(context: Context, @StringRes stringRes: Int): Builder {
            return setPositiveButtonText(context.getString(stringRes))
        }

        fun setPositiveButtonText(text: String?): Builder {
            args.putString(ARG_POSITIVE_BUTTON_TEXT, text)
            return this
        }

        fun setPositiveButtonDeepLink(deepLink: String?): Builder {
            args.putString(ARG_POSITIVE_BUTTON_DEEP_LINK, deepLink)
            return this
        }

        fun setCancelable(cancelable: Boolean): Builder {
            args.putBoolean(ARG_CANCELABLE, cancelable)
            return this
        }

        fun setClicker(clicker: TwoButtonClicker?): Builder {
            this.clicker = clicker
            return this
        }

        fun setClickPositiveOnCancel(clickOnCancel: Boolean): Builder {
            args.putBoolean(ARG_CLICK_POSITIVE_ON_CANCEL, clickOnCancel)
            return this
        }

        fun setClickNegativeOnCancel(clickOnCancel: Boolean): Builder {
            args.putBoolean(ARG_CLICK_NEGATIVE_ON_CANCEL, clickOnCancel)
            return this
        }

        fun build(): NotificationDialogFragment {
            return build(NotificationDialogFragment())
        }

        fun <T : NotificationDialogFragment> build(base: T): T {
            base.arguments = args
            base.clicker = clicker
            return base
        }
    }

    private fun AlertDialog.Builder.setTitleIfNeeded(title: String?, useDialogTitle: Boolean) {
        if (title.isNotSemanticallyNull() && useDialogTitle) {
            setTitle(title)
        }
    }

    companion object {
        const val ARG_TITLE = "ARG_TITLE"

        const val ARG_MESSAGE = "ARG_MESSAGE"

        const val ARG_NEGATIVE_BUTTON_TEXT = "ARG_NEGATIVE_BUTTON_TEXT"
        const val ARG_POSITIVE_BUTTON_TEXT = "ARG_POSITIVE_BUTTON_TEXT"

        const val ARG_NEGATIVE_BUTTON_DEEP_LINK = "ARG_NEGATIVE_BUTTON_DEEP_LINK"
        const val ARG_POSITIVE_BUTTON_DEEP_LINK = "ARG_POSITIVE_BUTTON_DEEP_LINK"

        const val ARG_CANCELABLE = "ARG_CANCELABLE"

        const val ARG_CLICK_POSITIVE_ON_CANCEL = "ARG_CLICK_POSITIVE_ONCANCEL"
        const val ARG_CLICK_NEGATIVE_ON_CANCEL = "ARG_CLICK_NEGATIVE_ONCANCEL"

        const val ARG_CUSTOM_VIEW_RES_ID = "ARG_CUSTOM_VIEW_RES_ID"

        const val ARG_IMAGE_RES_ID = "ARG_IMAGE_RES_ID"
        const val ARG_IMAGE_URL = "ARG_IMAGE_URL"
    }
}