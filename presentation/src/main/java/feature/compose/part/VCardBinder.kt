package feature.compose.part

import android.content.ContentUris
import android.content.Context
import android.view.View
import com.moez.QKSMS.R
import common.Navigator
import common.util.Colors
import common.util.extensions.setBackgroundTint
import common.util.extensions.setTint
import ezvcard.Ezvcard
import feature.compose.BubbleUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.mms_vcard_list_item.view.*
import mapper.CursorToPartImpl
import model.Message
import model.MmsPart
import util.extensions.isVCard
import util.extensions.mapNotNull

class VCardBinder(
        private val context: Context,
        private val navigator: Navigator,
        private val theme: Colors.Theme
) : PartBinder {

    override val partLayout = R.layout.mms_vcard_list_item

    override fun canBindPart(part: MmsPart) = part.isVCard()

    override fun bindPart(view: View, part: MmsPart, message: Message, canGroupWithPrevious: Boolean, canGroupWithNext: Boolean) {
        val uri = ContentUris.withAppendedId(CursorToPartImpl.CONTENT_URI, part.id)

        view.setOnClickListener { navigator.saveVcard(uri) }
        view.vCardBackground.setBackgroundResource(BubbleUtils.getBubble(canGroupWithPrevious, canGroupWithNext, message.isMe()))

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

}