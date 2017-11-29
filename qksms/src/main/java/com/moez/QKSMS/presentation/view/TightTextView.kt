package com.moez.QKSMS.presentation.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

class TightTextView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0)
    : TextView(context, attrs, defStyle) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        layout?.let {
            val maxLineWidth = (0 until layout.lineCount)
                    .asSequence()
                    .map { layout.getLineMax(it) }
                    .max() ?: 0f

            val width = (Math.ceil(maxLineWidth.toDouble()).toInt() + compoundPaddingLeft + compoundPaddingRight)
            val height = measuredHeight
            setMeasuredDimension(width, height)
        }
    }

}