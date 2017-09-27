package com.moez.QKSMS.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.moez.QKSMS.R
import com.moez.QKSMS.dagger.AppComponentManager
import com.moez.QKSMS.data.model.Contact
import com.moez.QKSMS.data.repository.ContactRepository
import com.moez.QKSMS.util.extensions.getColorCompat
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.avatar_view.view.*
import javax.inject.Inject


class AvatarView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : FrameLayout(context, attrs, defStyleAttr) {

    var contact: Contact? = null
        set(value) {
            field = value
            updateView()
        }

    @Inject lateinit var contactRepo: ContactRepository

    private var avatarRequest: Disposable? = null

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
        avatarRequest?.dispose()
        photo.setImageDrawable(null)
        initial.text = "?"

        contact?.let { contact ->
            avatarRequest = contactRepo.getAvatar(contact.photoUri).subscribe({ photo.setImageBitmap(it) }, {})
            initial.text = if (contact.name.isNotEmpty()) contact.name.substring(0, 1) else "?"
        }
    }
}