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
package feature.notificationprefs

import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import com.f2prateek.rx.preferences2.Preference
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.Navigator
import common.base.QkViewModel
import io.reactivex.Flowable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.schedulers.Schedulers
import repository.MessageRepository
import util.Preferences
import util.extensions.mapNotNull
import javax.inject.Inject

class NotificationPrefsViewModel @Inject constructor(
        private val intent: Intent,
        private val context: Context,
        private val messageRepo: MessageRepository,
        private val navigator: Navigator,
        private val prefs: Preferences
) : QkViewModel<NotificationPrefsView, NotificationPrefsState>(NotificationPrefsState()) {

    private val threadId = intent.extras?.getLong("threadId") ?: 0L

    private val notifications: Preference<Boolean>
    private val previews: Preference<Int>
    private val vibration: Preference<Boolean>
    private val ringtone: Preference<String>

    init {
        notifications = prefs.notifications(threadId)
        previews = prefs.notificationPreviews(threadId)
        vibration = prefs.vibration(threadId)
        ringtone = prefs.ringtone(threadId)

        disposables += Flowable.just(threadId)
                .mapNotNull { threadId -> messageRepo.getConversation(threadId) }
                .map { conversation -> conversation.getTitle() }
                .subscribeOn(Schedulers.io())
                .subscribe { title -> newState { it.copy(conversationTitle = title) } }

        disposables += notifications.asObservable()
                .subscribe { enabled -> newState { it.copy(notificationsEnabled = enabled) } }

        val previewLabels = context.resources.getStringArray(R.array.notification_preview_options)
        disposables += previews.asObservable()
                .subscribe { previewId ->
                    newState { it.copy(previewSummary = previewLabels[previewId], previewId = previewId) }
                }

        disposables += vibration.asObservable()
                .subscribe { enabled -> newState { it.copy(vibrationEnabled = enabled) } }

        disposables += ringtone.asObservable()
                .map { uriString ->
                    uriString.takeIf { it.isNotEmpty() }
                            ?.let(Uri::parse)
                            ?.let { uri -> RingtoneManager.getRingtone(context, uri) }?.getTitle(context)
                            ?: context.getString(R.string.settings_ringtone_none)
                }
                .subscribe { title -> newState { it.copy(ringtoneName = title) } }

        disposables += prefs.qkreply.asObservable()
                .subscribe { enabled -> newState { it.copy(qkReplyEnabled = enabled) } }

        disposables += prefs.qkreplyTapDismiss.asObservable()
                .subscribe { enabled -> newState { it.copy(qkReplyTapDismiss = enabled) } }
    }

    override fun bindView(view: NotificationPrefsView) {
        super.bindView(view)

        view.preferenceClickIntent
                .autoDisposable(view.scope())
                .subscribe {
                    when (it.id) {
                        R.id.notificationsO -> navigator.showNotificationChannel(threadId)

                        R.id.notifications -> notifications.set(!notifications.get())

                        R.id.notificationPreviews -> view.showPreviewModeDialog()

                        R.id.vibration -> vibration.set(!vibration.get())

                        R.id.ringtone -> view.showRingtonePicker(ringtone.get().takeIf { it.isNotEmpty() }?.let(Uri::parse))

                        R.id.qkreply -> prefs.qkreply.set(!prefs.qkreply.get())

                        R.id.qkreplyTapDismiss -> prefs.qkreplyTapDismiss.set(!prefs.qkreplyTapDismiss.get())
                    }
                }

        view.previewModeSelectedIntent
                .autoDisposable(view.scope())
                .subscribe { previews.set(it) }

        view.ringtoneSelectedIntent
                .autoDisposable(view.scope())
                .subscribe { ringtone -> this.ringtone.set(ringtone) }
    }
}