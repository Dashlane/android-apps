package com.dashlane.ui.widgets.view

import android.content.Context
import android.content.res.ColorStateList
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.cardview.widget.CardView
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.withStyledAttributes
import com.dashlane.ui.R
import kotlinx.parcelize.Parcelize

class ExpandableCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.expandableCardViewStyle
) : CardView(
    ContextThemeWrapper(context, R.style.ThemeOverlay_Dashlane_Shape),
    attrs,
    defStyleAttr
) {
    var expanded: Boolean = false
        private set
    private val contentLayout: ViewGroup
    private var arrow: ImageView? = null
    private lateinit var expandableLayout: View
    private var expandListener: ExpandListener? = null

    init {
        inflate(context, R.layout.expandablecardview, this)
        contentLayout = findViewById(R.id.contentLayout)
        init(attrs, defStyleAttr)
    }

    override fun onSaveInstanceState(): Parcelable {
        return SaveState(
            superState = super.onSaveInstanceState(),
            expanded = expanded
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as? SaveState)?.let { saveState ->
            super.onRestoreInstanceState(saveState.superState)
            setExpanded(saveState.expanded, false)
        }
    }

    fun setOnExpandListener(listener: ExpandListener) {
        expandListener = listener
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        backgroundTintList = ColorStateList.valueOf(context.getColor((R.color.container_agnostic_neutral_supershy)))
        context.withStyledAttributes(
            attrs, R.styleable.ExpandableCardView, defStyleAttr, R.style.Widget_Dashlane_ExpandableCardView
        ) {
            val headerId = getResourceIdOrThrow(R.styleable.ExpandableCardView_headerLayout)
            inflate(context, headerId, contentLayout)
            val expandableId = getResourceIdOrThrow(R.styleable.ExpandableCardView_expandableLayout)
            expandableLayout = LayoutInflater.from(context).inflate(expandableId, null)
            contentLayout.addView(expandableLayout)
            val arrowId = getResourceId(R.styleable.ExpandableCardView_arrow, 0)
            arrow = findViewById(arrowId)
            arrow?.imageTintList = ColorStateList.valueOf(context.getColor(R.color.text_neutral_standard))
            setExpanded(expanded, false)
            setOnClickListener {
                switchExpand()
            }
        }
    }

    private fun switchExpand(withAnimation: Boolean = true) {
        setExpanded(!expanded, withAnimation)
    }

    fun setExpanded(expanded: Boolean, withAnimation: Boolean = true) {
        if (expanded) {
            if (withAnimation) arrow?.animate()?.rotation(0f) else arrow?.rotation = 0f
            expandableLayout.visibility = View.VISIBLE
            arrow?.contentDescription = context.getString(R.string.and_accessibility_action_collapse)
        } else {
            if (withAnimation) arrow?.animate()?.rotation(180f) else arrow?.rotation = 180f
            expandableLayout.visibility = View.GONE
            arrow?.contentDescription = context.getString(R.string.and_accessibility_action_expand)
        }
        this.expanded = expanded
        expandListener?.onExpandChange(expanded)
    }

    fun interface ExpandListener {
        fun onExpandChange(expanded: Boolean)
    }

    @Parcelize
    private data class SaveState(
        val superState: Parcelable?,
        val expanded: Boolean
    ) : Parcelable
}