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
package feature.compose

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
import com.jakewharton.rxbinding2.widget.editorActions
import com.jakewharton.rxbinding2.widget.textChanges
import com.moez.QKSMS.R
import common.base.QkAdapter
import common.base.QkViewHolder
import common.util.Colors
import common.util.extensions.dpToPx
import common.util.extensions.setBackgroundTint
import common.util.extensions.showKeyboard
import io.reactivex.functions.Predicate
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.contact_chip.view.*
import model.Contact
import javax.inject.Inject

class ChipsAdapter @Inject constructor(private val context: Context, private val colors: Colors) : QkAdapter<Contact>() {

    companion object {
        private const val TYPE_EDIT_TEXT = 0
        private const val TYPE_ITEM = 1
    }

    private val hint: String = context.getString(R.string.title_compose)
    private val editText = View.inflate(context, R.layout.chip_input_list_item, null) as EditText

    var view: RecyclerView? = null
    val chipDeleted: PublishSubject<Contact> = PublishSubject.create<Contact>()
    val textChanges = editText.textChanges()
    val actions = editText.editorActions()

    /**
     * On certain devices, not returning false in this predicate for number entry will result in
     * the user not being able to enter numbers into the text field. As a result, we have to only
     * pass the events that we're looking for
     */
    val keyEvents = editText.keys(Predicate { event ->
        event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_DEL
    })

    init {
        val wrap = ViewGroup.LayoutParams.WRAP_CONTENT
        editText.layoutParams = FlexboxLayoutManager.LayoutParams(wrap, wrap).apply {
            minWidth = 56.dpToPx(context)
            flexGrow = 8f
        }

        editText.hint = hint
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

                // If the contact's name is empty, try to display a phone number instead
                // The contacts provided here should only have one number
                view.name.text = if (contact.name.isNotBlank()) {
                    contact.name
                } else {
                    contact.numbers.firstOrNull { it.address.isNotBlank() }?.address ?: ""
                }

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
