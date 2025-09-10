package com.dashlane.autofill.api.model

import android.app.assist.AssistStructure.WindowNode
import android.text.InputType
import android.view.autofill.AutofillId
import com.dashlane.autofill.formdetector.model.AutoFillViewNode

class AutoFillApiWindowNode(
    private val node: WindowNode,
    override val packageName: String,
) : AutoFillViewNode {
    override val autofillId: AutofillId?
        get() = null
    override val viewIdResourceName: String?
        get() = null
    override val childCount: Int
        get() = 1
    override val isLogin: Boolean
        get() = false
    override val isPassword: Boolean
        get() = false
    override val contentDescription: CharSequence?
        get() = null
    override val hint: CharSequence?
        get() = null
    override val text: CharSequence?
        get() = null
    override val htmlTag: CharSequence?
        get() = null
    override val isVisibleToUser: Boolean
        get() = true
    override val className: CharSequence
        get() = node.javaClass.name
    override val isFocused: Boolean
        get() = false
    override val superParent: AutoFillViewNode
        get() = this
    override val url: String?
        get() = null
    override val inputType: Int
        get() = InputType.TYPE_NULL
    override val hasHtmlAttributes: Boolean
        get() = false

    override fun getHtmlAttribute(name: String): String? = null

    override fun getChild(index: Int): AutoFillViewNode {
        return AutoFillApiViewNode.newInstance(node.rootViewNode, null, packageName)
    }
}