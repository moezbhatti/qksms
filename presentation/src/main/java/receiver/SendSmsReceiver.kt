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
package receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import injection.appComponent
import interactor.RetrySending
import repository.MessageRepository
import javax.inject.Inject

class SendSmsReceiver : BroadcastReceiver() {

    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var retrySending: RetrySending

    override fun onReceive(context: Context, intent: Intent) {
        appComponent.inject(this)

        val id = intent.getLongExtra("id", 0L)
        messageRepo.getMessage(id)?.let { message ->
            val result = goAsync()
            retrySending.execute(message) { result.finish() }
        }
    }

}