package feature.compose.part

import android.view.View
import model.Message
import model.MmsPart

interface PartBinder {

    val partLayout: Int

    fun canBindPart(part: MmsPart): Boolean

    fun bindPart(view: View, part: MmsPart, message: Message, canGroupWithPrevious: Boolean, canGroupWithNext: Boolean)

}