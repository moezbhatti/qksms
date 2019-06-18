/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.common.widget

import android.content.Context
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.util.AttributeSet
import java.util.regex.Pattern


val GPS_NW_DMS : Pattern = Pattern.compile(
        "N([0-9]+)[^a-zA-Z0-9]+([0-9]+)[^a-zA-Z0-9]+([0-9]+)[^yza-wA-Z0-9]+W([0-9]+)[^a-zA-Z0-9]+([0-9]+)[^a-zA-Z0-9]+([0-9]+)"
)

private fun ordinalFromStrings(s_degrees: String, s_minutes: String, s_seconds: String): String {
    val degrees = Integer.parseInt(s_degrees).toFloat()
    val minutes = Integer.parseInt(s_minutes).toFloat()
    val seconds = Integer.parseInt(s_seconds).toFloat()
    return java.lang.Float.toString(degrees + minutes / 60.0f + seconds / 3600.0f)
}


class TightTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : QkTextView(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // Get a non-null copy of the layout, if available. Then ensure we've got multiple lines
        val layout = layout ?: return
        if (layout.lineCount <= 1) {
            return
        }

        val maxLineWidth = (0 until layout.lineCount)
                .map(layout::getLineWidth)
                .max() ?: 0f

        val width = Math.ceil(maxLineWidth.toDouble()).toInt() + compoundPaddingLeft + compoundPaddingRight
        if (width < measuredWidth) {
            val widthSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.getMode(widthMeasureSpec))
            super.onMeasure(widthSpec, heightMeasureSpec)
        }
    }


    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        if(text != null) {
            var added = false
            var not_linked = false
            val spannable = SpannableString.valueOf(this.text)
            val m = this.movementMethod
            if ((m == null || m !is LinkMovementMethod) && this.linksClickable) {
                not_linked = true
                added = Linkify.addLinks(spannable, GPS_NW_DMS, "geo:0,0?q=", null, null,
                        { matcher, _ ->
                            ordinalFromStrings(matcher.group(1), matcher.group(2), matcher.group(3)) +
                            ",-" +
                            ordinalFromStrings(matcher.group(4), matcher.group(5), matcher.group(6))
                        })
            }
            if (added) {
                this.movementMethod = LinkMovementMethod.getInstance()
                this.text = spannable
            } else if (not_linked) {
                Linkify.addLinks(this, Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS or Linkify.WEB_URLS)
            }
        }
    }
}