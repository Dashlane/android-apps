package android.view.accessibility

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.dashlane.autofill.accessibility.AutoFillAccessibilityViewNode
import com.dashlane.autofill.formdetector.model.AutoFillViewNode
import com.dashlane.util.tryOrNull

fun AccessibilityEvent.toAutoFillViewNode(): AutoFillViewNode? {
    val viewNode = tryOrNull { (this as AccessibilityRecord).source } ?: return null
    return AutoFillAccessibilityViewNode.newInstance(AccessibilityNodeInfoCompat.wrap(viewNode))
}