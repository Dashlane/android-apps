package com.dashlane.item.subview

import android.content.res.ColorStateList
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.item.subview.edit.ItemAuthenticatorEditSubView
import com.dashlane.item.subview.edit.ItemEditPasswordWithStrengthSubView
import com.dashlane.item.subview.edit.ItemEditSpaceSubView
import com.dashlane.item.subview.edit.ItemEditValueBooleanSubView
import com.dashlane.item.subview.edit.ItemEditValueDateSubView
import com.dashlane.item.subview.edit.ItemEditValueListNonDefaultSubView
import com.dashlane.item.subview.edit.ItemEditValueListSubView
import com.dashlane.item.subview.edit.ItemEditValueNumberSubView
import com.dashlane.item.subview.edit.ItemEditValueRawSubView
import com.dashlane.item.subview.edit.ItemEditValueTextSubView
import com.dashlane.item.subview.provider.SubViewFactory
import com.dashlane.item.subview.readonly.EmptyLinkedServicesSubView
import com.dashlane.item.subview.readonly.ItemAuthenticatorReadSubView
import com.dashlane.item.subview.readonly.ItemClickActionSubView
import com.dashlane.item.subview.readonly.ItemInfoboxSubView
import com.dashlane.item.subview.readonly.ItemLinkedServicesSubView
import com.dashlane.item.subview.readonly.ItemMetaReadValueDateTimeSubView
import com.dashlane.item.subview.readonly.ItemPasswordSafetySubView
import com.dashlane.item.subview.readonly.ItemReadSpaceSubView
import com.dashlane.item.subview.readonly.ItemReadValueBooleanSubView
import com.dashlane.item.subview.readonly.ItemReadValueDateSubView
import com.dashlane.item.subview.readonly.ItemReadValueListSubView
import com.dashlane.item.subview.readonly.ItemReadValueNumberSubView
import com.dashlane.item.subview.readonly.ItemReadValueRawSubView
import com.dashlane.item.subview.readonly.ItemReadValueTextSubView
import com.dashlane.item.subview.view.AuthenticatorViewProvider
import com.dashlane.item.subview.view.ButtonInputProvider
import com.dashlane.item.subview.view.CollectionListViewProvider
import com.dashlane.item.subview.view.DatePickerInputProvider
import com.dashlane.item.subview.view.EditTextInputProvider
import com.dashlane.item.subview.view.InfoboxViewProvider
import com.dashlane.item.subview.view.MetaTextViewProvider
import com.dashlane.item.subview.view.PasswordSafetyViewProvider
import com.dashlane.item.subview.view.PasswordWithStrengthViewProvider
import com.dashlane.item.subview.view.SpaceSelectorProvider
import com.dashlane.item.subview.view.SpinnerInputProvider
import com.dashlane.item.subview.view.SpinnerNoDefaultInputProvider
import com.dashlane.item.subview.view.SwitchInputProvider
import com.dashlane.item.subview.view.TextInputLayoutProvider
import com.dashlane.util.addOnFieldVisibilityToggleListener
import com.dashlane.util.addTextChangedListener
import com.dashlane.util.dpToPx
import com.dashlane.util.getThemeAttrDrawable
import com.dashlane.util.getThemeAttrResourceId

class ViewFactory(private val activity: AppCompatActivity) {
    fun makeView(itemSubView: ItemSubView<*>): View {
        return when (itemSubView) {
            is ItemLinkedServicesSubView -> createLinkedServicesTextSubView(itemSubView)
            is EmptyLinkedServicesSubView -> FrameLayout(activity)
            is ItemAuthenticatorEditSubView -> createAuthenticatorEditSubView(itemSubView)
            is ItemAuthenticatorReadSubView -> createAuthenticatorReadSubView(itemSubView)
            is ItemClickActionSubView -> createClickActionSubview(itemSubView)
            is ItemReadValueDateSubView -> createReadDateSubview(itemSubView)
            is ItemEditValueDateSubView -> createEditDateSubview(itemSubView)
            is ItemMetaReadValueDateTimeSubView -> createMetaDateTimeSubview(itemSubView)
            is ItemEditPasswordWithStrengthSubView -> createEditPasswordWithStrengthSubview(
                itemSubView
            )
            is ItemEditValueBooleanSubView -> createEditValueBooleanSubview(itemSubView)
            is ItemReadValueBooleanSubView -> createReadValueBooleanSubview(itemSubView)
            is ItemEditValueListSubView -> createEditValueListSubview(itemSubView)
            is ItemEditValueListNonDefaultSubView -> createEditValueListNonDefaultSubview(
                itemSubView
            )
            is ItemReadValueListSubView -> createReadValueListSubview(itemSubView)
            is ItemPasswordSafetySubView -> createPasswordSafetySubview(itemSubView)
            is ItemEditValueTextSubView -> createEditValueTextSubView(itemSubView)
            is ItemReadValueTextSubView -> createReadValueTextSubView(itemSubView)
            is ItemEditValueNumberSubView -> createEditValueNumberSubView(itemSubView)
            is ItemReadValueNumberSubView -> createReadValueNumberSubView(itemSubView)
            is ItemEditSpaceSubView -> createEditSpaceSubView(itemSubView)
            is ItemReadSpaceSubView -> createReadSpaceSubView(itemSubView)
            is ItemEditValueRawSubView -> createItemEditValueRawSubView(itemSubView)
            is ItemReadValueRawSubView -> createItemReadValueRawSubView(itemSubView)
            is ItemInfoboxSubView -> createInfoboxSubView(itemSubView)
            is ItemCollectionListSubView -> createItemCollectionListSubView(itemSubView)
            else -> throw IllegalArgumentException("Unknown item type: ${itemSubView::class.java}")
        }
    }

    private fun createPasswordSafetySubview(item: ItemPasswordSafetySubView) =
        PasswordSafetyViewProvider.create(activity, item.header)

    private fun createReadValueTextSubView(item: ItemReadValueTextSubView) =
        TextInputLayoutProvider.create(
            activity,
            item.header,
            item.value,
            protected = item.protected,
            allowReveal = item.allowReveal,
            multiline = item.multiline,
            coloredCharacter = item.coloredCharacter
        ).apply {
            addOnFieldVisibilityToggleListener {
                item.protectedStateListener.invoke(it)
            }
        }

    private fun createEditValueTextSubView(item: ItemEditValueTextSubView) =
        TextInputLayoutProvider.create(
            activity, item.hint, item.value, true, item.protected,
            item.allowReveal, item.suggestions, multiline = item.multiline,
            coloredCharacter = item.coloredCharacter
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
            item.header,
            item.value,
            protected = item.protected
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
            item.hint,
            item.value,
            true,
            item.protected,
            suggestions = item.suggestions
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

    private fun createItemReadValueRawSubView(item: ItemReadValueRawSubView) =
        EditTextInputProvider.create(activity, item.hint, item.value, item.textSize, false).apply {
            addTextChangedListener {
                afterTextChanged {
                    val newValue = it.toString()
                    item.notifyValueChanged(newValue)
                }
            }
        }

    private fun createItemEditValueRawSubView(item: ItemEditValueRawSubView) =
        EditTextInputProvider.create(activity, item.hint, item.value, item.textSize, true).apply {
            addTextChangedListener {
                afterTextChanged {
                    val newValue = it.toString()
                    item.notifyValueChanged(newValue)
                }
            }
        }

    private fun createReadValueBooleanSubview(item: ItemReadValueBooleanSubView) =
        SwitchInputProvider.create(activity, item.header, item.description, item.value, false)

    private fun createEditSpaceSubView(item: ItemEditSpaceSubView) =
        SpaceSelectorProvider.create(activity, item.value, item.values, true, item) { selectedIndex ->
            val newValue = item.values[selectedIndex]
            item.notifyValueChanged(newValue)
        }

    private fun createReadSpaceSubView(item: ItemReadSpaceSubView) =
        SpaceSelectorProvider.create(activity, item.value, item.values, false, null)

    private fun createEditValueListSubview(item: ItemEditValueListSubView) =
        SpinnerInputProvider.create(activity, item.title, item.value, item.values, true, item) { selectedIndex ->
            val newValue = item.values[selectedIndex]
            item.notifyValueChanged(newValue)
        }

    private fun createEditValueListNonDefaultSubview(item: ItemEditValueListNonDefaultSubView) =
        SpinnerNoDefaultInputProvider.create(activity, item.title, item.value, item.values) { newValue ->
            item.notifyValueChanged(newValue)
        }

    private fun createReadValueListSubview(item: ItemReadValueListSubView) =
        SpinnerInputProvider.create(activity, item.title, item.value, item.values, false)

    private fun createEditPasswordWithStrengthSubview(item: ItemEditPasswordWithStrengthSubView): View {
        val textInputLayout = createEditValueTextSubView(item)
        return PasswordWithStrengthViewProvider.create(activity, textInputLayout)
    }

    private fun createReadDateSubview(item: ItemReadValueDateSubView) =
        DatePickerInputProvider.create(activity, item.hint, item.formattedDate)

    private fun createEditDateSubview(item: ItemEditValueDateSubView) =
        DatePickerInputProvider.create(activity, item.hint, item.formattedDate).apply {
            DatePickerInputProvider.setClickListener(
                activity = activity,
                textInputLayout = this,
                originalDate = item.value
            ) { newDate -> item.notifyValueChanged(newDate) }
        }

    private fun createMetaDateTimeSubview(item: ItemMetaReadValueDateTimeSubView) =
        MetaTextViewProvider.create(
            context = activity,
            header = item.header,
            value = item.formattedDate
        )

    private fun createClickActionSubview(item: ItemClickActionSubView) = FrameLayout(activity).apply {
        addView(
            ButtonInputProvider.create(activity, item.value, item.iconResId, item.mood, item.intensity) {
                item.clickAction.invoke()
            },
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                item.gravity
            )
        )
    }

    private fun createAuthenticatorReadSubView(item: ItemAuthenticatorReadSubView) =
        AuthenticatorViewProvider.create(activity, item.title, item.value)

    private fun createAuthenticatorEditSubView(item: ItemAuthenticatorEditSubView) =
        if (item.value == null) {
            AuthenticatorViewProvider.createActivate(activity, activity.getString(item.title))
        } else {
            AuthenticatorViewProvider.create(activity, activity.getString(item.title), item.value!!)
        }

    private fun createInfoboxSubView(item: ItemInfoboxSubView) =
        InfoboxViewProvider.create(
            activity = activity,
            mood = item.mood,
            title = item.value,
            primaryButton = item.primaryButton,
            secondaryButton = item.secondaryButton
        )

    private fun createLinkedServicesTextSubView(itemSubView: ItemLinkedServicesSubView): View {
        return LinearLayout(activity).apply {
            isClickable = true
            background = context.getThemeAttrDrawable(android.R.attr.selectableItemBackground)
            setOnClickListener {
                itemSubView.action.onClickAction(activity)
            }
            contentDescription = context.getString(R.string.multi_domain_credentials_title)
            addView(
                TextView(activity).apply {
                    text = itemSubView.value
                    gravity = Gravity.CENTER_VERTICAL
                    setTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceBody1))
                    setTextColor(context.getColor(R.color.text_brand_standard))
                },
                LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    1F
                ).apply {
                    leftMargin = context.dpToPx(4)
                    setPadding(0, context.dpToPx(4), 0, context.dpToPx(4))
                }
            )
            addView(
                ImageView(activity).apply {
                    setImageResource(itemSubView.action.icon)
                    imageTintList = ColorStateList.valueOf(
                        context.getColor(R.color.text_brand_standard)
                    )
                    gravity = Gravity.CENTER_VERTICAL
                },
                LinearLayout.LayoutParams(
                    context.dpToPx(24),
                    context.dpToPx(24)
                ).apply {
                    rightMargin = context.dpToPx(12)
                    leftMargin = context.dpToPx(8)
                }
            )
        }
    }

    private fun createItemCollectionListSubView(item: ItemCollectionListSubView) =
        CollectionListViewProvider.create(activity, item)
}
