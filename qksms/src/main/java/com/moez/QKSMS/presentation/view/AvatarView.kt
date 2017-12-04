package com.moez.QKSMS.presentation.view

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.util.GlideApp
import com.moez.QKSMS.common.util.ThemeManager
import com.moez.QKSMS.data.model.Contact
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.avatar_view.view.*
import javax.inject.Inject

class AvatarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    @Inject lateinit var themeManager: ThemeManager

    private val disposables = CompositeDisposable()

    var contact: Contact? = null
        set(value) {
            field = value
            updateView()
        }

    init {
        View.inflate(context, R.layout.avatar_view, this)
        AppComponentManager.appComponent.inject(this)

        setBackgroundResource(R.drawable.circle)
        clipToOutline = true
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        disposables += themeManager.color
                .subscribe { color -> background.setTint(color) }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disposables.clear()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        if (!isInEditMode) {
            updateView()
        }
    }

    private fun updateView() {
        if (contact?.name.orEmpty().isNotEmpty()) {
            initial.text = contact?.name?.substring(0, 1)
            icon.visibility = GONE
        } else {
            initial.text = null
            icon.visibility = VISIBLE
        }

        photo.setImageDrawable(null)
        contact?.let { contact ->
            GlideApp.with(photo).load(PhoneNumberUtils.stripSeparators(contact.address)).into(photo)
        }
    }
}