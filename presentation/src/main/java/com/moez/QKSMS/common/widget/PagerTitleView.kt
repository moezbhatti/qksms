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
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.viewpager.widget.ViewPager
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.forEach
import com.moez.QKSMS.common.util.extensions.resolveThemeColor
import com.moez.QKSMS.extensions.Optional
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.repository.ConversationRepository
import com.uber.autodispose.android.ViewScopeProvider
import com.uber.autodispose.autoDisposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.tab_view.view.*
import javax.inject.Inject

class PagerTitleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    @Inject lateinit var colors: Colors
    @Inject lateinit var conversationRepo: ConversationRepository

    private val recipientId: Subject<Long> = BehaviorSubject.create()

    var pager: ViewPager? = null
        set(value) {
            if (field !== value) {
                field = value
                recreate()
            }
        }

    init {
        if (!isInEditMode) appComponent.inject(this)
    }

    fun setRecipientId(id: Long) {
        recipientId.onNext(id)
    }

    private fun recreate() {
        removeAllViews()

        pager?.adapter?.count?.forEach { position ->
            val view = LayoutInflater.from(context).inflate(R.layout.tab_view, this, false)
            view.label.text = pager?.adapter?.getPageTitle(position)
            view.setOnClickListener { pager?.currentItem = position }

            addView(view)
        }

        childCount.forEach { index ->
            getChildAt(index).isActivated = index == pager?.currentItem
        }

        pager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                childCount.forEach { index ->
                    getChildAt(index).isActivated = index == position
                }
            }
        })
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val states = arrayOf(
                intArrayOf(android.R.attr.state_activated),
                intArrayOf(-android.R.attr.state_activated))

        recipientId
                .distinctUntilChanged()
                .map { recipientId -> Optional(conversationRepo.getRecipient(recipientId)) }
                .switchMap { recipient -> colors.themeObservable(recipient.value) }
                .map { theme ->
                    val textSecondary = context.resolveThemeColor(android.R.attr.textColorSecondary)
                    ColorStateList(states, intArrayOf(theme.theme, textSecondary))
                }
                .autoDisposable(ViewScopeProvider.from(this))
                .subscribe { colorStateList ->
                    childCount.forEach { index ->
                        (getChildAt(index) as? TextView)?.setTextColor(colorStateList)
                    }
                }
    }

}
