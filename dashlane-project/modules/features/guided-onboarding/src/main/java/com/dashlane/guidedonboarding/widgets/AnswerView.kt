package com.dashlane.guidedonboarding.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.core.text.HtmlCompat.FROM_HTML_MODE_LEGACY
import com.dashlane.guidedonboarding.R
import com.dashlane.util.isNotSemanticallyNull

internal class AnswerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        init(attrs)
    }

    var showDetail: Boolean = false
        set(value) {
            field = value
            val title = findViewById<TextView>(R.id.title)
            val detail = findViewById<TextView>(R.id.detail)
            detail.visibility = if (value && detail.text.isNotSemanticallyNull()) {
                View.VISIBLE
            } else {
                View.GONE
            }
            title.setTextColor(context.getColor(R.color.text_neutral_catchy))
        }

    private fun init(attrs: AttributeSet?) {
        inflate(context, R.layout.view_answer, this)
        isClickable = true

        setBackgroundResource(R.drawable.rounded_rectangle_background)

        val ta = context.obtainStyledAttributes(attrs, R.styleable.AnswerView)
        try {
            val titleRes = ta.getResourceId(R.styleable.AnswerView_answertitle, 0)
            if (titleRes > 0) findViewById<TextView>(R.id.title).setText(titleRes)

            val detailRes = ta.getResourceId(R.styleable.AnswerView_answerdetail, 0)
            if (detailRes > 0) {
                findViewById<TextView>(R.id.detail).text =
                    HtmlCompat.fromHtml(context.getString(detailRes), FROM_HTML_MODE_LEGACY)
            }
            showDetail = ta.getBoolean(R.styleable.AnswerView_answershowdetail, false)
        } finally {
            ta.recycle()
        }
    }
}