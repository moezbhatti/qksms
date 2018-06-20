package com.moez.QKSMS.feature.compose.part

import android.view.View
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.model.MmsPart

interface PartBinder {

    val partLayout: Int

    fun canBindPart(part: MmsPart): Boolean

    fun bindPart(view: View, part: MmsPart, message: Message, canGroupWithPrevious: Boolean, canGroupWithNext: Boolean)

}