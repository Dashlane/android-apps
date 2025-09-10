package com.dashlane.autofill.formdetector.model

import android.view.autofill.AutofillId

interface AutoFillViewNode {
    val autofillId: AutofillId?
    val viewIdResourceName: String?
    val childCount: Int
    val packageName: CharSequence?
    val isLogin: Boolean
    val isPassword: Boolean
    val contentDescription: CharSequence?
    val hint: CharSequence?
    val text: CharSequence?
    val htmlTag: CharSequence?
    val isVisibleToUser: Boolean
    val className: CharSequence?
    val isFocused: Boolean
    val superParent: AutoFillViewNode?
    val url: String?
    val inputType: Int
    val hasHtmlAttributes: Boolean

    fun getHtmlAttribute(name: String): String?

    fun getChild(index: Int): AutoFillViewNode?
}