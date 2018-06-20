package feature.compose

import android.content.ContentUris
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.moez.QKSMS.R
import common.Navigator
import common.base.QkAdapter
import common.base.QkViewHolder
import common.util.Colors
import common.util.GlideApp
import common.util.extensions.forwardTouches
import common.util.extensions.setBackgroundTint
import common.util.extensions.setTint
import common.util.extensions.setVisible
import common.widget.BubbleImageView
import ezvcard.Ezvcard
import feature.compose.BubbleUtils.canGroup
import feature.compose.BubbleUtils.getBubble
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.message_list_item_in.view.*
import kotlinx.android.synthetic.main.mms_preview_list_item.view.*
import kotlinx.android.synthetic.main.mms_vcard_list_item.view.*
import mapper.CursorToPartImpl
import model.Message
import model.MmsPart
import util.extensions.isImage
import util.extensions.isVCard
import util.extensions.isVideo
import util.extensions.mapNotNull

class PartsAdapter(
        private val context: Context,
        private val navigator: Navigator,
        private val theme: Colors.Theme
) : QkAdapter<MmsPart>() {

    companion object {
        private const val TYPE_THUMBNAIL = 0
        private const val TYPE_VCARD = 1
    }

    private lateinit var message: Message
    private var previous: Message? = null
    private var next: Message? = null
    private var messageView: View? = null
    private var bodyVisible: Boolean = true

    fun setData(message: Message, previous: Message?, next: Message?, messageView: View) {
        this.message = message
        this.previous = previous
        this.next = next
        this.messageView = messageView
        this.bodyVisible = messageView.body.visibility == View.VISIBLE
        this.data = message.parts.filter { it.isImage() || it.isVideo() || it.isVCard() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QkViewHolder {
        return QkViewHolder(LayoutInflater.from(parent.context).inflate(when (viewType) {
            TYPE_THUMBNAIL -> R.layout.mms_preview_list_item
            TYPE_VCARD -> R.layout.mms_vcard_list_item
            else -> 0
        }, parent, false))
    }

    override fun onBindViewHolder(holder: QkViewHolder, position: Int) {
        val part = data[position]
        val view = holder.itemView

        messageView?.let(view::forwardTouches)

        val canGroupWithPrevious = canGroup(message, previous) || position > 0
        val canGroupWithNext = canGroup(message, next) || position < itemCount - 1 || bodyVisible

        when {
            part.isImage() || part.isVideo() -> bindMedia(view, part, canGroupWithPrevious, canGroupWithNext)
            part.isVCard() -> bindVCard(view, part, canGroupWithPrevious, canGroupWithNext)
        }
    }

    private fun bindMedia(view: View, part: MmsPart, canGroupWithPrevious: Boolean, canGroupWithNext: Boolean) {
        view.video.setVisible(part.isVideo())
        view.setOnClickListener { navigator.showMedia(part.id) }

        view.thumbnail.bubbleStyle = when {
            !canGroupWithPrevious && canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_FIRST else BubbleImageView.Style.IN_FIRST
            canGroupWithPrevious && canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_MIDDLE else BubbleImageView.Style.IN_MIDDLE
            canGroupWithPrevious && !canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_LAST else BubbleImageView.Style.IN_LAST
            else -> BubbleImageView.Style.ONLY
        }

        GlideApp.with(context).load(part.getUri()).fitCenter().into(view.thumbnail)
    }

    private fun bindVCard(view: View, part: MmsPart, canGroupWithPrevious: Boolean, canGroupWithNext: Boolean) {
        val uri = ContentUris.withAppendedId(CursorToPartImpl.CONTENT_URI, part.id)

        view.setOnClickListener { navigator.saveVcard(uri) }
        view.vCardBackground.setBackgroundResource(getBubble(canGroupWithPrevious, canGroupWithNext, message.isMe()))

        Observable.just(uri)
                .map(context.contentResolver::openInputStream)
                .mapNotNull { inputStream -> inputStream.use { Ezvcard.parse(it).first() } }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { vcard -> view.name?.text = vcard.formattedName.value }

        if (!message.isMe()) {
            view.vCardBackground.setBackgroundTint(theme.theme)
            view.vCardAvatar.setTint(theme.textPrimary)
            view.name.setTextColor(theme.textPrimary)
            view.label.setTextColor(theme.textTertiary)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val part = data[position]
        return when {
            part.isImage() || part.isVideo() -> TYPE_THUMBNAIL
            part.isVCard() -> TYPE_VCARD
            else -> -1
        }
    }

}