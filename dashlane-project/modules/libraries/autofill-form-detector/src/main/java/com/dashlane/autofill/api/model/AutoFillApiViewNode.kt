package com.dashlane.autofill.api.model

import android.app.assist.AssistStructure
import android.view.autofill.AutofillId
import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import java.util.Arrays

class AutoFillApiViewNode private constructor(
    val viewNode: AssistStructure.ViewNode,
    private val parentViewNode: AssistStructure.ViewNode?,
    override val packageName: String?,
) : AutoFillViewNode {
    override val autofillId: AutofillId?
        get() = viewNode.autofillId
    override val viewIdResourceName: String?
        get() = viewNode.idEntry
    override val childCount: Int
        get() = viewNode.childCount
    override val isPassword: Boolean
        get() = false
    override val isLogin: Boolean
        get() = false
    override val contentDescription: CharSequence?
        get() = viewNode.contentDescription
    override val hint: CharSequence?
        get() = viewNode.hint
    override val text: CharSequence?
        get() = viewNode.text
    override val htmlTag: CharSequence?
        get() = viewNode.htmlInfo?.tag
    override val isVisibleToUser: Boolean
        get() = true
    override val className: CharSequence?
        get() = viewNode.className
    override val isFocused: Boolean
        get() = viewNode.isFocused
    override val superParent: AutoFillViewNode?
        get() = parentViewNode?.let { newInstance(parentViewNode, null, packageName) }
    override val url: String?
        get() = viewNode.webDomain
    override val inputType: Int
        get() = viewNode.inputType
    override val hasHtmlAttributes: Boolean
        get() = viewNode.htmlInfo?.attributes?.isNotEmpty() ?: false

    override fun getHtmlAttribute(name: String): String? =
        viewNode.htmlInfo?.attributes?.firstOrNull { it.first == name }?.second

    override fun toString(): String {
        return "APIWrapper{" +
            "mViewNode=" + viewNodeString +
            ", mPackageName='" + packageName + '\'' +
            '}'
    }

    private val viewNodeString: String
        get() {
            val stringBuilder = StringBuilder()
            stringBuilder.append("{")
            stringBuilder.append("""getAutofillHints=${Arrays.toString(viewNode.autofillHints)}""".trimIndent())
            stringBuilder.append("""getAutofillType=${viewNode.autofillType}""".trimIndent())
            stringBuilder.append("""getAutofillValue=${viewNode.autofillValue}""".trimIndent())
            stringBuilder.append("""getAutofillHints=${Arrays.toString(viewNode.autofillHints)}""".trimIndent())
            stringBuilder.append("""getInputType=${viewNode.inputType}""".trimIndent())
            stringBuilder.append("""getIdEntry=${viewNode.idEntry}""".trimIndent())
            return stringBuilder.toString()
        }

    override fun getChild(index: Int): AutoFillViewNode {
        return newInstance(viewNode.getChildAt(index), viewNode, packageName)
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun newInstance(
            viewNode: AssistStructure.ViewNode,
            parentViewNode: AssistStructure.ViewNode?,
            packageName: String? = viewNode.idPackage,
        ): AutoFillApiViewNode {
            return AutoFillApiViewNode(viewNode, parentViewNode, packageName)
        }
    }
}