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
package presentation.compose

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import com.google.android.flexbox.FlexboxLayoutManager
import com.jakewharton.rxbinding2.view.keys
import com.jakewharton.rxbinding2.widget.textChanges
import com.moez.QKSMS.R
import common.util.Colors
import common.util.extensions.dpToPx
import common.util.extensions.setBackgroundTint
import common.util.extensions.showKeyboard
import data.model.Contact
import presentation.common.base.QkAdapter
import presentation.common.base.QkViewHolder
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.contact_chip.view.*
import javax.inject.Inject

class ChipsAdapter @Inject constructor(private val context: Context, private val colors: Colors) : QkAdapter<Contact>() {

    companion object {
        private val TYPE_EDIT_TEXT = 0
        private val TYPE_ITEM = 1
    }

    private val hint: String = context.getString(R.string.title_compose)
    private val editText = View.inflate(context, R.layout.chip_input_list_item, null) as EditText

    var view: RecyclerView? = null
    val chipDeleted: PublishSubject<Contact> = PublishSubject.create<Contact>()
    val textChanges = editText.textChanges()

    init {
        val wrap = ViewGroup.LayoutParams.WRAP_CONTENT
        editText.layoutParams = FlexboxLayoutManager.LayoutParams(wrap, wrap).apply {
            minWidth = 56.dpToPx(context)
            flexGrow = 8f
        }

        editText.hint = hint
        editText.keys()
                .filter { event -> event.action == KeyEvent.ACTION_DOWN }
                .filter { event -> event.keyCode == KeyEvent.KEYCODE_DEL }
                .subscribe {
                    if (itemCount > 1 && editText.text.isEmpty()) {
                        chipDeleted.onNext(getItem(itemCount - 2))
                    }
                }
    }

    override fun onDatasetChanged() {
        editText.text = null
        editText.hint = if (itemCount == 1) hint else null

        if (itemCount != 2) {
            editText.showKeyboard()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        TYPE_EDIT_TEXT -> QkViewHolder(editText)

        else -> {
            val view = LayoutInflater.from(context).inflate(R.layout.contact_chip, parent, false)
            colors.composeBackground.subscribe { color -> view.content.setBackgroundTint(color) }

            QkViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_ITEM -> {
                val contact = getItem(position)
                val view = holder.itemView

                view.avatar.contact = contact
                view.name.text = contact.name

                view.setOnClickListener { showDetailedChip(contact) }
            }
        }
    }

    override fun getItemCount() = super.getItemCount() + 1

    override fun getItemViewType(position: Int) = if (position == itemCount - 1) TYPE_EDIT_TEXT else TYPE_ITEM

    private fun showDetailedChip(contact: Contact) {
        val detailedChipView = DetailedChipView(context)
        detailedChipView.setContact(contact)

        val rootView = view?.rootView as ViewGroup

        val layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)

        layoutParams.topMargin = 24.dpToPx(context)
        layoutParams.marginStart = 56.dpToPx(context)

        rootView.addView(detailedChipView, layoutParams)
        detailedChipView.show()

        detailedChipView.setOnDeleteListener {
            chipDeleted.onNext(contact)
            detailedChipView.hide()
        }
    }

    override fun areItemsTheSame(old: Contact, new: Contact): Boolean {
        return old.lookupKey == new.lookupKey
    }
}
