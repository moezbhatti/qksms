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
package com.moez.QKSMS.presentation.common.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.Colors
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import javax.inject.Inject

open class QkTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : TextView(context, attrs) {

    @Inject lateinit var colors: Colors

    private val disposables = CompositeDisposable()
    private var textColorObservable: Observable<Int>? = null

    init {
        appComponent.inject(this)

        context.obtainStyledAttributes(attrs, R.styleable.QkTextView)?.run {
            textColorObservable = when (getInt(R.styleable.QkTextView_textColor, -1)) {
                0 -> colors.textPrimary
                1 -> colors.textSecondary
                2 -> colors.textTertiary
                3 -> colors.textPrimaryOnTheme
                4 -> colors.textSecondaryOnTheme
                5 -> colors.textTertiaryOnTheme
                else -> null
            }
            recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        textColorObservable?.let { observable ->
            disposables += observable.subscribe { color ->
                setTextColor(color)
                setLinkTextColor(color)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        disposables.dispose()
    }

}