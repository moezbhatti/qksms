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
package com.moez.QKSMS.feature.compose.part

import android.view.View
import com.moez.QKSMS.R
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setTint
import com.moez.QKSMS.extensions.isAudio
import com.moez.QKSMS.model.Message
import com.moez.QKSMS.model.MmsPart
import kotlinx.android.synthetic.main.mms_audio_list_item.view.*

class AudioBinder(private val theme: Colors.Theme) : PartBinder {

    override val partLayout = R.layout.mms_audio_list_item

    override fun canBindPart(part: MmsPart) = part.isAudio()

    override fun bindPart(view: View, part: MmsPart, message: Message, canGroupWithPrevious: Boolean, canGroupWithNext: Boolean) {
        view.play.setBackgroundTint(theme.theme)
        view.play.setTint(theme.textPrimary)
    }

}