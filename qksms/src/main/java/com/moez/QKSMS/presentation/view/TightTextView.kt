package com.moez.QKSMS.presentation.view

import android.content.Context
import android.util.AttributeSet

class TightTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : QkTextView(context, attrs) {

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