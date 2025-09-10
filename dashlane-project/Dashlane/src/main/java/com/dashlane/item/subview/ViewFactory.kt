package com.dashlane.item.subview

import android.text.InputType
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.item.subview.edit.ItemEditSpaceSubView
import com.dashlane.item.subview.edit.ItemEditValueBooleanSubView
import com.dashlane.item.subview.edit.ItemEditValueDateSubView
import com.dashlane.item.subview.edit.ItemEditValueListNonDefaultSubView
import com.dashlane.item.subview.edit.ItemEditValueListSubView
import com.dashlane.item.subview.edit.ItemEditValueNumberSubView
import com.dashlane.item.subview.edit.ItemEditValueTextSubView
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.readonly.ItemClickActionSubView
import com.dashlane.item.subview.readonly.ItemInfoboxSubView
import com.dashlane.item.subview.readonly.ItemMetaReadValueDateTimeSubView
import com.dashlane.item.subview.readonly.ItemReadSpaceSubView
import com.dashlane.item.subview.readonly.ItemReadValueBooleanSubView
import com.dashlane.item.subview.readonly.ItemReadValueDateSubView
import com.dashlane.item.subview.readonly.ItemReadValueListSubView
import com.dashlane.item.subview.readonly.ItemReadValueNumberSubView
import com.dashlane.item.subview.readonly.ItemReadValueTextSubView
import com.dashlane.item.subview.view.ButtonInputProvider
import com.dashlane.item.subview.view.DatePickerInputProvider
import com.dashlane.item.subview.view.InfoboxViewProvider
import com.dashlane.item.subview.view.MetaTextViewProvider
import com.dashlane.item.subview.view.SpaceSelectorProvider
import com.dashlane.item.subview.view.SpinnerInputProvider
import com.dashlane.item.subview.view.SpinnerNoDefaultInputProvider
import com.dashlane.item.subview.view.SwitchInputProvider
import com.dashlane.item.subview.view.TextInputLayoutProvider
import com.dashlane.lock.LockManager
import com.dashlane.util.Toaster
import com.dashlane.util.addOnFieldVisibilityToggleListener
import com.dashlane.util.addTextChangedListener

class ViewFactory(
    private val activity: AppCompatActivity,
    private val toaster: Toaster,
    private val lockManager: LockManager,
) {
    fun makeView(itemSubView: ItemSubView<*>): View {
        return when (itemSubView) {
            is ItemClickActionSubView -> createClickActionSubview(itemSubView)
            is ItemReadValueDateSubView -> createReadDateSubview(itemSubView)
            is ItemEditValueDateSubView -> createEditDateSubview(itemSubView)
            is ItemMetaReadValueDateTimeSubView -> createMetaDateTimeSubview(itemSubView)
            is ItemEditValueBooleanSubView -> createEditValueBooleanSubview(itemSubView)
            is ItemReadValueBooleanSubView -> createReadValueBooleanSubview(itemSubView)
            is ItemEditValueListSubView -> createEditValueListSubview(itemSubView)
            is ItemEditValueListNonDefaultSubView -> createEditValueListNonDefaultSubview(itemSubView)
            is ItemReadValueListSubView -> createReadValueListSubview(itemSubView)
            is ItemEditValueTextSubView -> createEditValueTextSubView(itemSubView)
            is ItemReadValueTextSubView -> createReadValueTextSubView(itemSubView)
            is ItemEditValueNumberSubView -> createEditValueNumberSubView(itemSubView)
            is ItemReadValueNumberSubView -> createReadValueNumberSubView(itemSubView)
            is ItemEditSpaceSubView -> createEditSpaceSubView(itemSubView)
            is ItemReadSpaceSubView -> createReadSpaceSubView(itemSubView)
            is ItemInfoboxSubView -> createInfoboxSubView(itemSubView)
            else -> throw IllegalArgumentException("Unknown item type: ${itemSubView::class.java}")
        }
    }

    private fun createReadValueTextSubView(item: ItemReadValueTextSubView) =
        TextInputLayoutProvider.create(
            activity,
            lockManager,
            item.header,
            item.value,
            protected = item.protected,
            allowReveal = item.allowReveal,
            multiline = item.multiline,
            coloredCharacter = item.coloredCharacter,
        ).apply {
            addOnFieldVisibilityToggleListener {
                item.protectedStateListener.invoke(it)
            }
        }

    private fun createEditValueTextSubView(item: ItemEditValueTextSubView) =
        TextInputLayoutProvider.create(
            activity,
            lockManager,
            item.hint,
            item.value,
            true,
            item.protected,
            item.allowReveal,
            item.suggestions,
            multiline = item.multiline,
            coloredCharacter = item.coloredCharacter,
        ).apply {
            editText!!.addTextChangedListener {
                afterTextChanged {
                    val newValue = it.toString()
                    item.notifyValueChanged(newValue)
                }
            }
            if (item.protected) {
                addOnFieldVisibilityToggleListener {
                    item.protectedStateListener.invoke(it)
                }
            }
        }

    private fun createReadValueNumberSubView(item: ItemReadValueNumberSubView) =
        TextInputLayoutProvider.create(
            activity,
            lockManager,
            item.header,
            item.value,
            protected = item.protected,
        ).apply {
            if (item.protected) {
                addOnFieldVisibilityToggleListener {
                    item.protectedStateListener.invoke(it)
                }
            }
        }

    private fun createEditValueNumberSubView(item: ItemEditValueNumberSubView) =
        TextInputLayoutProvider.create(
            activity,
            lockManager,
            item.hint,
            item.value,
            true,
            item.protected,
            suggestions = item.suggestions,
        ).apply {
            editText!!.apply {
                addTextChangedListener {
                    afterTextChanged {
                        val newValue = it.toString()
                        item.notifyValueChanged(newValue)
                    }
                }
                inputType = when (item.inputType) {
                    SubViewFactory.INPUT_TYPE_PHONE -> InputType.TYPE_CLASS_PHONE
                    SubViewFactory.INPUT_TYPE_NUMBER -> InputType.TYPE_CLASS_NUMBER
                    else -> throw IllegalArgumentException("Invalid InputType for ItemEditValueNumberSubView")
                }
            }
            if (item.protected) {
                addOnFieldVisibilityToggleListener {
                    item.protectedStateListener.invoke(it)
                }
            }
        }

    private fun createEditValueBooleanSubview(item: ItemEditValueBooleanSubView) =
        SwitchInputProvider.create(activity, item.header, item.description, item.value, true) { value ->
            item.notifyValueChanged(value)
        }

    private fun createReadValueBooleanSubview(item: ItemReadValueBooleanSubView) =
        SwitchInputProvider.create(activity, item.header, item.description, item.value, false)

    private fun createEditSpaceSubView(item: ItemEditSpaceSubView) =
        SpaceSelectorProvider.create(activity, item.value, item.values, true, item, toaster) { selectedIndex ->
            val newValue = item.values[selectedIndex]
            item.notifyValueChanged(newValue)
        }

    private fun createReadSpaceSubView(item: ItemReadSpaceSubView) =
        SpaceSelectorProvider.create(activity, item.value, item.values, false, null, toaster)

    private fun createEditValueListSubview(item: ItemEditValueListSubView) =
        SpinnerInputProvider.create(activity, item.title, item.value, item.values, true, item) { selectedIndex ->
            val newValue = item.values[selectedIndex]
            item.notifyValueChanged(newValue)
        }

    private fun createEditValueListNonDefaultSubview(item: ItemEditValueListNonDefaultSubView) =
        SpinnerNoDefaultInputProvider.create(activity, lockManager, item.title, item.value, item.values) { newValue ->
            item.notifyValueChanged(newValue)
        }

    private fun createReadValueListSubview(item: ItemReadValueListSubView) =
        SpinnerInputProvider.create(activity, item.title, item.value, item.values, false)

    private fun createReadDateSubview(item: ItemReadValueDateSubView) =
        DatePickerInputProvider.create(activity, lockManager, item.hint, item.formattedDate)

    private fun createEditDateSubview(item: ItemEditValueDateSubView) =
        DatePickerInputProvider.create(activity, lockManager, item.hint, item.formattedDate).apply {
            DatePickerInputProvider.setClickListener(
                activity = activity,
                textInputLayout = this,
                originalDate = item.value,
            ) { newDate -> item.notifyValueChanged(newDate) }
        }

    private fun createMetaDateTimeSubview(item: ItemMetaReadValueDateTimeSubView) =
        MetaTextViewProvider.create(
            context = activity,
            header = item.header,
            value = item.formattedDate,
        )

    private fun createClickActionSubview(item: ItemClickActionSubView) = FrameLayout(activity).apply {
        addView(
            ButtonInputProvider.create(activity, item.value, item.iconResId, item.mood, item.intensity) {
                item.clickAction.invoke()
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                item.gravity,
            ),
        )
    }

    private fun createInfoboxSubView(item: ItemInfoboxSubView) =
        InfoboxViewProvider.create(
            activity = activity,
            mood = item.mood,
            title = item.value,
            description = item.description,
            primaryButton = item.primaryButton,
            secondaryButton = item.secondaryButton,
        )
}