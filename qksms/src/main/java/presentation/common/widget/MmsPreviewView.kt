package presentation.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.moez.QKSMS.R
import common.util.GlideApp
import common.util.extensions.setVisible
import data.model.MmsPart
import kotlinx.android.synthetic.main.mms_preview_view.view.*

class MmsPreviewView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {

    var parts: List<MmsPart> = ArrayList()
        set(value) {
            field = value
            updateView()
        }

    init {
        View.inflate(context, R.layout.mms_preview_view, this)
    }

    fun updateView() {
        val images = parts.mapNotNull { it.image }
        setVisible(images.isNotEmpty())

        images.firstOrNull()?.let {
            GlideApp.with(context).load(it).fitCenter().into(image)
        }
    }

}