package com.moez.QKSMS.presentation.compose

import android.content.Context
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.RelativeLayout
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.data.model.Contact
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.contact_chip_detailed.view.*
import javax.inject.Inject


class DetailedChipView(context: Context) : RelativeLayout(context) {

    @Inject lateinit var colors: Colors

    private val disposables = CompositeDisposable()

    init {
        View.inflate(context, R.layout.contact_chip_detailed, this)
        appComponent.inject(this)

        setOnClickListener { hide() }

        visibility = View.GONE

        isFocusable = true
        isFocusableInTouchMode = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        disposables += colors.theme
                .subscribe { color -> card.setBackgroundTint(color) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposables.clear()
    }

    fun setContact(contact: Contact) {
        avatar.contact = contact
        name.text = contact.name
        info.text = contact.numbers.map { it.address }.toString()
    }

    fun show() {
        startAnimation(AlphaAnimation(0f, 1f).apply { duration = 200 })

        visibility = View.VISIBLE
        requestFocus()
        isClickable = true
    }

    fun hide() {
        startAnimation(AlphaAnimation(1f, 0f).apply { duration = 200 })

        visibility = View.GONE
        clearFocus()
        isClickable = false
    }

    fun setOnDeleteListener(listener: (View) -> Unit) {
        delete.setOnClickListener(listener)
    }

}
