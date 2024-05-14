package com.dashlane.ui.widgets.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.dashlane.R
import com.dashlane.databinding.IncludeImportMethodItemBinding

class ImportMethodItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var binding: IncludeImportMethodItemBinding

    init {
        binding = IncludeImportMethodItemBinding.inflate(LayoutInflater.from(context), this, true)

        val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.ImportMethodItem)
        try {
            val titleRes = styledAttributes.getResourceId(R.styleable.ImportMethodItem_methodname, 0)
            if (titleRes > 0) {
                binding.importMethodName.setText(titleRes)
            }
            val iconRes = styledAttributes.getResourceId(R.styleable.ImportMethodItem_methodicon, 0)
            if (iconRes > 0) {
                binding.importMethodIcon.setImageResource(iconRes)
            }
        } finally {
            styledAttributes.recycle()
        }
    }

    fun setBadgeVisibility(visible: Boolean) {
        binding.importMethodBadge.visibility = if (visible) {
            VISIBLE
        } else {
            GONE
        }
    }
}