package com.dashlane.item

import android.content.Intent
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.dashlane.R
import com.dashlane.authenticator.AuthenticatorIntro.Companion.RESULT_OTP
import com.dashlane.authenticator.AuthenticatorLogger
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.item.AuthenticatorViewProxy
import com.dashlane.design.theme.color.Intensity
import com.dashlane.design.theme.color.Mood
import com.dashlane.item.BaseUiUpdateListener.Companion.BOTTOM_SHEET_DIALOG_TAG
import com.dashlane.item.BaseUiUpdateListener.Companion.NFC_DIALOG_ERROR_TAG
import com.dashlane.item.BaseUiUpdateListener.Companion.NFC_DIALOG_SUCCESS_TAG
import com.dashlane.item.BaseUiUpdateListener.Companion.NFC_DIALOG_TAG
import com.dashlane.item.delete.DeleteVaultItemFragment
import com.dashlane.item.header.ItemHeader
import com.dashlane.item.subview.Action
import com.dashlane.item.subview.ItemSubView
import com.dashlane.item.subview.ItemSubViewWithActionWrapper
import com.dashlane.item.subview.ViewFactory
import com.dashlane.item.subview.action.MenuAction
import com.dashlane.item.subview.action.authenticator.ActivateRemoveAuthenticatorAction
import com.dashlane.item.subview.edit.ItemAuthenticatorEditSubView
import com.dashlane.item.subview.edit.ItemEditPasswordWithStrengthSubView
import com.dashlane.item.subview.edit.ItemEditValueDateSubView
import com.dashlane.item.subview.edit.ItemEditValueListSubView
import com.dashlane.item.subview.edit.ItemEditValueNumberSubView
import com.dashlane.item.subview.edit.ItemEditValueTextSubView
import com.dashlane.item.subview.readonly.ItemAuthenticatorReadSubView
import com.dashlane.item.subview.readonly.ItemPasswordSafetySubView
import com.dashlane.item.subview.readonly.ItemReadValueNumberSubView
import com.dashlane.item.subview.readonly.ItemReadValueTextSubView
import com.dashlane.item.subview.view.ButtonInputProvider
import com.dashlane.item.subview.view.DatePickerInputProvider
import com.dashlane.item.subview.view.SpinnerInputProvider
import com.dashlane.navigation.Navigator
import com.dashlane.passwordstrength.PasswordStrengthEvaluator
import com.dashlane.passwordstrength.borderColorRes
import com.dashlane.passwordstrength.getShortTitle
import com.dashlane.passwordstrength.textColorRes
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.ui.adapter.SpinnerAdapterDefaultValueString
import com.dashlane.ui.credential.passwordgenerator.StrengthLevelUpdater
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment
import com.dashlane.ui.screens.fragments.userdata.CredentialViewPasswordSafety
import com.dashlane.ui.widgets.view.tintProgressDrawable
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.computeStatusBarColor
import com.dashlane.util.getColorOn
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.util.getThemeAttrDrawable
import com.dashlane.util.setContentTint
import com.dashlane.util.statusBarColor
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.skocken.presentation.viewproxy.BaseViewProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class ItemEditViewViewProxy(
    val activity: AppCompatActivity,
    private val viewFactory: ViewFactory,
    private val lifecycleOwner: LifecycleOwner,
    private val navigator: Navigator,
    private val authenticatorLogger: AuthenticatorLogger,
    private val passwordStrengthEvaluator: PasswordStrengthEvaluator,
    private val vaultDataQuery: VaultDataQuery
) :
    BaseViewProxy<ItemEditViewContract.Presenter>(activity),
    ItemEditViewContract.View {

    private val allDialogsTags = listOf(
        DatePickerInputProvider.DATE_PICKER_DIALOG_TAG,
        DeleteVaultItemFragment.DELETE_DIALOG_TAG,
        SAVE_DIALOG_TAG,
        NFC_DIALOG_TAG,
        NFC_DIALOG_SUCCESS_TAG,
        NFC_DIALOG_ERROR_TAG,
        BOTTOM_SHEET_DIALOG_TAG
    )

    init {
        
        allDialogsTags.forEach {
            (activity.supportFragmentManager.findFragmentByTag(it) as? DialogFragment)?.dismissAllowingStateLoss()
        }

        
        activity.supportFragmentManager.setFragmentResultListener(
            DeleteVaultItemFragment.DELETE_VAULT_ITEM_RESULT,
            lifecycleOwner
        ) { _, bundle ->
            val isItemDeleted = bundle.getBoolean(DeleteVaultItemFragment.DELETE_VAULT_ITEM_SUCCESS)
            if (isItemDeleted) {
                activity.finish()
            }
        }
    }

    override val listener: ItemEditViewContract.View.UiUpdateListener
        get() = object : BaseUiUpdateListener(activity, navigator) {

            override fun notifySubViewChanged(itemSubView: ItemSubView<*>) {
                val view = views[itemSubView] ?: return
                when (itemSubView) {
                    is ItemEditValueDateSubView -> configureDateSubView(itemSubView, view)
                    is ItemEditPasswordWithStrengthSubView -> configurePasswordWithStrengthSubView(
                        itemSubView,
                        view
                    )

                    is ItemReadValueTextSubView -> configureReadValueTextSubView(itemSubView, view)
                    is ItemEditValueTextSubView -> configureEditValueTextSubView(itemSubView, view)
                    is ItemReadValueNumberSubView -> configureReadValueNumberSubView(
                        itemSubView,
                        view
                    )

                    is ItemEditValueNumberSubView -> configureEditValueNumberSubView(
                        itemSubView,
                        view
                    )

                    is ItemPasswordSafetySubView -> passwordSafety?.setPassword(itemSubView.value)
                    is ItemEditValueListSubView -> configureValueListSubView(
                        itemSubView,
                        view as LinearLayout
                    )

                    is ItemAuthenticatorEditSubView -> configureAuthenticatorSubview(
                        itemSubView,
                        view
                    )
                }
            }

            override fun notifyHeaderChanged(itemHeader: ItemHeader, editMode: Boolean) {
                setHeader(itemHeader, editMode)
            }

            override fun notifyColorChanged(color: Int) {
                setDominantColor(color)
            }

            override fun notifyPotentialBarCodeScan(
                requestCode: Int,
                resultCode: Int,
                data: Intent?
            ) {
                val otp = data?.getParcelableExtraCompat<Otp>(RESULT_OTP) ?: return
                
                views.filter { (it.key as? ItemSubViewWithActionWrapper)?.itemSubView is ItemAuthenticatorEditSubView || it.key is ItemAuthenticatorEditSubView }
                    .forEach {
                        val subview =
                            (it.key as? ItemSubViewWithActionWrapper)?.itemSubView ?: it.key
                        (subview as ItemAuthenticatorEditSubView).notifyValueChanged(otp)
                        listener.notifySubViewChanged(subview)
                    }
            }

            override fun requestFocus(subview: ItemSubView<*>) {
                views[subview]?.requestFocus()
            }

            override fun notifyDeleteClicked() = presenter.deleteClicked()

            override fun notifyRestorePassword() = presenter.restorePassword()

            override fun notifyOtpRefreshed(otp: Otp) = presenter.otpRefreshed(otp)
        }

    private val collapsingToolbarLayout =
        findViewByIdEfficient<CollapsingToolbarLayout>(R.id.collapsingToolbar)!!
    private val scrollView = findViewByIdEfficient<NestedScrollView>(R.id.item_edit_scrollView)
    private val subViewContainer = findViewByIdEfficient<ViewGroup>(R.id.subview_container)!!
    private val toolbar = findViewByIdEfficient<Toolbar>(R.id.view_toolbar)!!
    private val title = findViewByIdEfficient<TextView>(R.id.view_action_bar_title)!!
    private val views = mutableMapOf<ItemSubView<*>, View>()
    private val actionsMenu = mutableMapOf<Action, MenuItem>()
    private val toolbarContentColor = context.getColor(R.color.text_neutral_standard)
    private var headerColor: Int = context.getColor(R.color.container_agnostic_neutral_standard)
    private val headerButtonColor: Int
        get() = if (presenter.isSecureNote) context.getColorOn(headerColor) else toolbarContentColor
    private var passwordSafety: CredentialViewPasswordSafety? = null
    private var strengthLevelUpdater: StrengthLevelUpdater? = null
    override var isToolbarCollapsed: Boolean = false
    private var resetPasswordScoreJob: Job? = null

    override fun setConfiguration(
        screenConfiguration: ScreenConfiguration,
        isEditMode: Boolean,
        isToolbarCollapsed: Boolean,
        isChangingMode: Boolean
    ) {
        this.isToolbarCollapsed = isToolbarCollapsed
        screenConfiguration.itemHeader?.let {
            setHeader(it, isEditMode)
        }
        setSubViews(screenConfiguration.itemSubViews, isEditMode)

        if (isChangingMode) {
            
            val parent = subViewContainer.parent
            if (parent is NestedScrollView) {
                parent.fullScroll(View.FOCUS_UP)
            }
        }
    }

    override fun setMenus(menuActions: List<MenuAction>, menu: Menu) {
        actionsMenu.clear()
        menuActions.forEach {
            menu.add(it.text).apply {
                setShowAsAction(it.displayFlags)
                isCheckable = it.checkable
                if (isCheckable) {
                    isChecked = it.checked
                    setMenuItemCheckState(this, it.icon)
                } else {
                    setIcon(it.icon)
                }

                icon = icon?.mutate()?.also { ic -> ic.setTint(headerButtonColor) }
            }.also { item ->
                actionsMenu[it] = item
            }
        }
    }

    override fun selectMenuItem(item: MenuItem): Boolean {
        val menuAction = actionsMenu.filter { it.value == item }
        menuAction.forEach {
            val action = it.key
            if (item.isCheckable) {
                item.isChecked = !item.isChecked
                setMenuItemCheckState(item, action.icon)
                item.icon = item.icon?.mutate()?.also { ic -> ic.setTint(headerButtonColor) }
            }
            action.onClickAction(activity)
        }
        return menuAction.isNotEmpty()
    }

    override fun askForSave(action: (Boolean) -> Unit) {
        NotificationDialogFragment.Builder().setTitle(activity.getString(R.string.save_item_))
            .setMessage(activity.getString(R.string.would_you_like_to_save_the_item_))
            .setNegativeButtonText(activity.getString(R.string.discart))
            .setPositiveButtonText(activity.getString(R.string.dialog_save_item_save_button))
            .setCancelable(true)
            .setClickPositiveOnCancel(false)
            .setClicker(object : NotificationDialogFragment.TwoButtonClicker {

                override fun onNegativeButton() {
                    action.invoke(false)
                }

                override fun onPositiveButton() {
                    action.invoke(true)
                }
            })
            .build()
            .show(activity.supportFragmentManager, SAVE_DIALOG_TAG)
    }

    override fun showConfirmDeleteDialog(itemId: String, isShared: Boolean) {
        navigator.goToDeleteVaultItem(itemId, isShared)
    }

    override fun showSaveConfirmation() {
        SnackbarUtils.showSnackbar(
            activity.findViewById(android.R.id.content) as View,
            activity.getString(R.string.multi_domain_credentials_update_validation),
            Snackbar.LENGTH_SHORT
        )
    }

    fun setDominantColor(@ColorInt dominantColor: Int) {
        headerColor = dominantColor
        collapsingToolbarLayout.setContentScrimColor(dominantColor)
        collapsingToolbarLayout.setBackgroundColor(dominantColor)
        toolbar.setBackgroundColor(dominantColor)
        activity.statusBarColor = computeStatusBarColor(dominantColor)
        title.setTextColor(context.getColor(R.color.text_neutral_catchy))
        toolbar.setContentTint(headerButtonColor)
    }

    private fun setMenuItemCheckState(item: MenuItem, icon: Int) {
        val stateListDrawable = context.resources.getDrawable(icon, null)
        val state =
            if (item.isChecked) arrayOf(android.R.attr.state_checked) else arrayOf(android.R.attr.state_empty)
        stateListDrawable.state = state.toIntArray()
        item.icon = stateListDrawable.current
    }

    private fun setHeader(itemHeader: ItemHeader, isEditMode: Boolean) {
        val toolbarLogo = findViewByIdEfficient<ImageView>(R.id.toolbar_logo)!!
        val toolbarIcon = findViewByIdEfficient<ImageView>(R.id.toolbar_icon)!!
        val appBar = findViewByIdEfficient<AppBarLayout>(R.id.view_app_bar)!!
        title.text = itemHeader.title
        title.setTextColor(toolbarContentColor)
        activity.setSupportActionBar(toolbar)
        activity.supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            itemHeader.image?.let {
                appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
                    val scroll = abs(verticalOffset) - appBarLayout.totalScrollRange
                    if (scroll == 0) {
                        
                        toolbarIcon.visibility = View.VISIBLE
                        isToolbarCollapsed = true
                    } else {
                        
                        toolbarIcon.visibility = View.GONE
                        isToolbarCollapsed = false
                    }
                }
                toolbarIcon.setImageDrawable(it)
                toolbarLogo.setImageDrawable(it)
                toolbarIcon.contentDescription =
                    context.getString(R.string.and_accessibility_domain_item_logo, itemHeader.title)
                toolbarLogo.contentDescription =
                    context.getString(R.string.and_accessibility_domain_item_logo, itemHeader.title)
            }
        }
        appBar.setExpanded(!isEditMode && !isToolbarCollapsed, true)
        ViewCompat.setNestedScrollingEnabled(scrollView as View, itemHeader.image != null)
        activity.invalidateOptionsMenu()
    }

    private fun setSubViews(itemSubView: List<ItemSubView<*>>, isEditMode: Boolean) {
        subViewContainer.removeAllViews()
        views.clear()
        itemSubView.forEach {
            createView(it, isEditMode).let { v ->
                subViewContainer.addView(
                    v,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        
                        topMargin = context.resources.getDimensionPixelSize(it.topMargin)
                    }
                )
            }
        }
        setDominantColor(headerColor)
        if (isEditMode) {
            
            views.values.lastOrNull { it is TextInputLayout }?.apply {
                (this as TextInputLayout).editText?.imeOptions = EditorInfo.IME_ACTION_DONE
            }
        }
    }

    private fun createView(itemSubView: ItemSubView<*>, isEditMode: Boolean = false): View {
        return when (itemSubView) {
            is ItemSubViewWithActionWrapper -> createSubViewWithAction(itemSubView)
            else -> viewFactory.makeView(itemSubView).also {
                when (itemSubView) {
                    is ItemEditPasswordWithStrengthSubView -> {
                        strengthLevelUpdater = StrengthLevelUpdater(lifecycleOwner.lifecycleScope)
                        configurePasswordWithStrengthSubView(itemSubView, it)
                    }

                    is ItemEditValueTextSubView -> configureEditValueTextSubView(itemSubView, it)
                    is ItemReadValueTextSubView -> configureReadValueTextSubView(itemSubView, it)
                    is ItemPasswordSafetySubView -> {
                        passwordSafety = CredentialViewPasswordSafety.newInstance(
                            it,
                            lifecycleOwner.lifecycleScope,
                            passwordStrengthEvaluator,
                            vaultDataQuery
                        )
                        configurePasswordSafetySubView(itemSubView, isEditMode)
                    }

                    is ItemEditValueDateSubView -> configureDateSubView(itemSubView, it)
                    is ItemEditValueListSubView -> configureValueListSubView(
                        itemSubView,
                        it as LinearLayout
                    )

                    is ItemAuthenticatorEditSubView,
                    is ItemAuthenticatorReadSubView -> setupAuthenticatorSubview(itemSubView, it)
                }
            }
        }.also {
            views[itemSubView] = it
        }
    }

    private fun createSubViewWithAction(wrapper: ItemSubViewWithActionWrapper<*>): View {
        val subview = createView(wrapper.itemSubView)
        val layout = LayoutInflater.from(context).inflate(
            R.layout.subview_with_action,
            subview as ViewGroup,
            false
        )
        layout.findViewById<FrameLayout>(R.id.view_container).addView(subview)

        with(wrapper.action) {
            val button = if (icon != -1) {
                ButtonInputProvider.createIconButton(
                    context,
                    context.getString(text),
                    icon,
                    Mood.Brand,
                    Intensity.Supershy
                ) {
                    onClickAction(activity)
                }
            } else {
                ButtonInputProvider.create(context, context.getString(text), icon, Mood.Brand, Intensity.Supershy) {
                    onClickAction(activity)
                }
            }

            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.RIGHT
            )
            layout.findViewById<FrameLayout>(R.id.view_action_container)
                .addView(button, layoutParams)
        }

        return layout
    }

    private fun configureReadValueTextSubView(item: ItemReadValueTextSubView, view: View) {
        val inputLayout = view as? TextInputLayout ?: return
        inputLayout.editText!!.setText(item.value)
        if (item.textColorResId != 0) {
            inputLayout.editText!!.setTextColor(item.textColorResId)
        }
    }

    private fun configureEditValueTextSubView(item: ItemEditValueTextSubView, view: View) {
        val inputLayout = view as? TextInputLayout ?: return

        inputLayout.hint = item.hint
        inputLayout.editText!!.setText(item.value)
        if (item.invisible) {
            inputLayout.visibility = View.GONE
        } else {
            inputLayout.visibility = View.VISIBLE
        }
    }

    private fun configureReadValueNumberSubView(item: ItemReadValueNumberSubView, view: View) {
        val inputLayout = view as? TextInputLayout ?: return
        inputLayout.editText!!.setText(item.value)
        if (item.textColorResId != 0) {
            inputLayout.editText!!.setTextColor(item.textColorResId)
        }
    }

    private fun configureEditValueNumberSubView(item: ItemEditValueNumberSubView, view: View) {
        val inputLayout = view as? TextInputLayout ?: return
        inputLayout.editText!!.setText(item.value)
        if (item.invisible) {
            inputLayout.visibility = View.GONE
        } else {
            inputLayout.visibility = View.VISIBLE
        }
    }

    fun configurePasswordWithStrengthSubView(
        itemSubView: ItemEditPasswordWithStrengthSubView,
        view: View
    ) {
        val inputLayout = view.findViewById(R.id.item_subview_text_input_layout) as? TextInputLayout
        if (inputLayout?.editText?.text?.toString() != itemSubView.value) {
            inputLayout?.editText?.setText(itemSubView.value, TextView.BufferType.EDITABLE)
        }
        val strengthLevel = view.findViewById<TextView>(R.id.item_subview_strength_level_textview)
        val strengthProgress =
            view.findViewById<ProgressBar>(R.id.item_subview_strength_level_progress_bar)
        if (itemSubView.value.isEmpty()) {
            strengthLevel.visibility = View.GONE
            strengthProgress.visibility = View.GONE
            inputLayout?.editText?.background =
                context.getThemeAttrDrawable(R.attr.editTextBackground)
        } else {
            strengthLevel.visibility = View.VISIBLE
            strengthProgress.visibility = View.VISIBLE
            inputLayout?.editText?.background = null
        }

        resetPasswordScoreJob?.cancel()
        resetPasswordScoreJob = lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            delay(500)
            
            
            strengthProgress.apply {
                isIndeterminate = true
                progress = 0
                strengthLevel.text = null
            }
        }

        strengthLevelUpdater?.updateWith(
            passwordStrengthEvaluator,
            itemSubView.value
        ) { strength ->
            resetPasswordScoreJob?.cancel()
            strengthLevel.apply {
                text = strength.getShortTitle(context)
                setTextColor(context.getColor(strength.textColorRes))
            }
            strengthProgress.apply {
                isIndeterminate = false
                
                progress = if (strength.percentScore == 0) 2 else strength.percentScore

                tintProgressDrawable(context.getColor(strength.borderColorRes))
            }
        }
    }

    private fun configurePasswordSafetySubView(
        item: ItemPasswordSafetySubView,
        isEditMode: Boolean
    ) {
        passwordSafety?.apply {
            setPassword(item.value)
            if (isEditMode) {
                onEditMode()
            } else {
                onViewMode()
            }
        }
    }

    private fun configureDateSubView(item: ItemEditValueDateSubView, view: View) {
        val inputLayout = view as? TextInputLayout ?: return
        inputLayout.editText?.setText(item.formattedDate, TextView.BufferType.EDITABLE)
        DatePickerInputProvider.setClickListener(
            activity,
            inputLayout,
            item.value
        ) { newDate -> item.notifyValueChanged(newDate) }
        if (item.invisible) {
            inputLayout.visibility = View.GONE
        } else {
            inputLayout.visibility = View.VISIBLE
        }
    }

    private fun configureValueListSubView(item: ItemEditValueListSubView, view: LinearLayout) {
        view.let {
            val newAdapter = SpinnerAdapterDefaultValueString(
                context,
                R.layout.spinner_item_dropdown,
                R.layout.spinner_item_preview,
                item.values,
                item.value
            )
            val promptText = context.getString(R.string.choose)

            val newSpinner = SpinnerInputProvider.createSpinner(
                context,
                item.value,
                true,
                promptText,
                newAdapter,
                null,
                null,
                null
            ) { selectedIndex ->
                val newValue = item.values[selectedIndex]
                item.notifyValueChanged(newValue)
            }

            
            view.findViewById<TextView>(R.id.item_subview_title).text = item.title

            
            val index = view.findViewById<Spinner>(R.id.item_subview_spinner).let {
                view.indexOfChild(it)
            }
            view.removeViewAt(index)
            view.addView(newSpinner, index)

            
            if (item.invisible) {
                view.visibility = View.GONE
            } else {
                view.visibility = View.VISIBLE
            }
        }
    }

    private fun setupAuthenticatorSubview(itemSubView: ItemSubView<*>, view: View) {
        if (itemSubView.value == null) return
        AuthenticatorViewProxy(
            view.findViewById(R.id.item_subview_textview),
            view.findViewById(R.id.item_subview_imageview),
            activity.lifecycleScope,
            itemSubView.value as Otp,
        ) { listener.notifyOtpRefreshed(it) }
    }

    private fun configureAuthenticatorSubview(item: ItemAuthenticatorEditSubView, view: View) {
        
        val subviewsWithAction =
            views.filter { (it.key as? ItemSubViewWithActionWrapper)?.itemSubView is ItemAuthenticatorEditSubView }
        val parent = if (subviewsWithAction.isEmpty()) {
            view as ViewGroup
        } else {
            subviewsWithAction.values.first() as ViewGroup
        }
        
        parent.removeAllViews()
        parent.addView(
            createView(
                ItemSubViewWithActionWrapper(
                    item,
                    ActivateRemoveAuthenticatorAction(item, listener, authenticatorLogger)
                )
            )
        )
    }

    companion object {
        const val SAVE_DIALOG_TAG = "save_dialog"
    }
}
