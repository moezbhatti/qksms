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
package presentation.feature.notificationprefs

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.net.toUri
import com.f2prateek.rx.preferences2.Preference
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.Preferences
import common.util.extensions.mapNotNull
import data.repository.MessageRepository
import io.reactivex.Flowable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import presentation.common.Navigator
import presentation.common.base.QkViewModel
import timber.log.Timber
import javax.inject.Inject

class NotificationPrefsViewModel(intent: Intent) : QkViewModel<NotificationPrefsView, NotificationPrefsState>(NotificationPrefsState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var messageRepo: MessageRepository
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var prefs: Preferences

    private val threadId = intent.extras?.getLong("threadId") ?: 0L

    private val notifications: Preference<Boolean>
    private val vibration: Preference<Boolean>
    private val ringtone: Preference<String>

    init {
        appComponent.inject(this)

        notifications = prefs.notifications(threadId)
        vibration = prefs.vibration(threadId)
        ringtone = prefs.ringtone(threadId)

        disposables += Flowable.just(threadId)
                .mapNotNull { threadId -> messageRepo.getConversation(threadId) }
                .map { conversation -> conversation.getTitle() }
                .subscribeOn(Schedulers.io())
                .subscribe { title -> newState { it.copy(conversationTitle = title) } }

        disposables += notifications.asObservable()
                .subscribe { enabled -> newState { it.copy(notificationsEnabled = enabled) } }

        disposables += vibration.asObservable()
                .subscribe { enabled -> newState { it.copy(vibrationEnabled = enabled) } }

        disposables += ringtone.asObservable()
                .map { uri -> uri.toUri() }
                .map { uri -> RingtoneManager.getRingtone(context, uri).getTitle(context) }
                .subscribe { title -> newState { it.copy(ringtoneName = title) } }
    }

    override fun bindView(view: NotificationPrefsView) {
        super.bindView(view)

        view.preferenceClickIntent
                .autoDisposable(view.scope())
                .subscribe {
                    Timber.v("Preference click: ${context.resources.getResourceName(it.id)}")

                    when (it.id) {
                        R.id.notifications -> notifications.set(!notifications.get())

                        R.id.vibration -> vibration.set(!vibration.get())

                        R.id.ringtone -> view.showRingtonePicker(Uri.parse(ringtone.get()))
                    }
                }

        view.ringtoneSelectedIntent
                .autoDisposable(view.scope())
                .subscribe { ringtone -> this.ringtone.set(ringtone) }
    }
}