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
package presentation.receiver

import android.net.Uri
import com.klinker.android.send_message.MmsReceivedReceiver
import common.di.appComponent
import interactor.SyncMessage
import javax.inject.Inject

class MmsReceivedReceiver : MmsReceivedReceiver() {

    @Inject lateinit var syncMessage: SyncMessage

    init {
        appComponent.inject(this)
    }

    override fun onMessageReceived(messageUri: Uri?) {
        super.onMessageReceived(messageUri)

        messageUri?.let { uri ->
            val pendingResult = goAsync()
            syncMessage.execute(uri) { pendingResult.finish() }
        }
    }

}