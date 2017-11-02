package com.moez.QKSMS.presentation.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.AppComponentManager
import com.moez.QKSMS.common.util.GlideApp
import com.moez.QKSMS.common.util.extensions.getColorCompat
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.repository.ContactRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.avatar_view.view.*
import javax.inject.Inject

class AvatarView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : FrameLayout(context, attrs, defStyleAttr) {

    @Inject lateinit var contactsRepo: ContactRepository

    var contact: Contact? = null
        set(value) {
            field = value
            updateView()
        }

    private var loadContact: Disposable? = null

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context) : this(context, null, 0)

    init {
        AppComponentManager.appComponent.inject(this)
        View.inflate(context, R.layout.avatar_view, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        setBackgroundResource(R.drawable.circle)
        clipToOutline = true
        backgroundTintList = ColorStateList.valueOf(context.getColorCompat(R.color.bubbleLight))

        if (!isInEditMode) {
            updateView()
        }
    }

    private fun updateView() {
        loadContact?.dispose()
        photo.setImageDrawable(null)
        initial.text = "?"

        contact?.let { contact ->
            initial.text = if (contact.name.isNotEmpty()) contact.name.substring(0, 1) else "?"
            loadContact = contactsRepo.findContactUri(contact.address)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ uri -> GlideApp.with(photo).load(uri).into(photo) }, { })
        }
    }
}