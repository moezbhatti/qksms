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
package com.moez.QKSMS.feature.notificationprefs

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import com.moez.QKSMS.R
import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.base.QkViewModel
import com.moez.QKSMS.extensions.mapNotNull
import com.moez.QKSMS.repository.ConversationRepository
import com.moez.QKSMS.util.Preferences
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Flowable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

class NotificationPrefsViewModel @Inject constructor(
        @Named("threadId") private val threadId: Long,
        private val context: Context,
        private val conversationRepo: ConversationRepository,
        private val navigator: Navigator,
        private val prefs: Preferences
) : QkViewModel<NotificationPrefsView, NotificationPrefsState>(NotificationPrefsState(threadId = threadId)) {

    private val notifications = prefs.notifications(threadId)
    private val previews = prefs.notificationPreviews(threadId)
    private val vibration = prefs.vibration(threadId)
    private val ringtone = prefs.ringtone(threadId)

    init {
        disposables += Flowable.just(threadId)
                .mapNotNull { threadId -> conversationRepo.getConversation(threadId) }
                .map { conversation -> conversation.getTitle() }
                .subscribeOn(Schedulers.io())
                .subscribe { title -> newState { copy(conversationTitle = title) } }

        disposables += notifications.asObservable()
                .subscribe { enabled -> newState { copy(notificationsEnabled = enabled) } }

        val previewLabels = context.resources.getStringArray(R.array.notification_preview_options)
        disposables += previews.asObservable()
                .subscribe { previewId ->
                    newState { copy(previewSummary = previewLabels[previewId], previewId = previewId) }
                }

        val actionLabels = context.resources.getStringArray(R.array.notification_actions)
        disposables += prefs.notifAction1.asObservable()
                .subscribe { previewId -> newState { copy(action1Summary = actionLabels[previewId]) } }

        disposables += prefs.notifAction2.asObservable()
                .subscribe { previewId -> newState { copy(action2Summary = actionLabels[previewId]) } }

        disposables += prefs.notifAction3.asObservable()
                .subscribe { previewId -> newState { copy(action3Summary = actionLabels[previewId]) } }

        disposables += vibration.asObservable()
                .subscribe { enabled -> newState { copy(vibrationEnabled = enabled) } }

        disposables += ringtone.asObservable()
                .map { uriString ->
                    uriString.takeIf { it.isNotEmpty() }
                            ?.let(Uri::parse)
                            ?.let { uri -> RingtoneManager.getRingtone(context, uri) }?.getTitle(context)
                            ?: context.getString(R.string.settings_ringtone_none)
                }
                .subscribe { title -> newState { copy(ringtoneName = title) } }

        disposables += prefs.qkreply.asObservable()
                .subscribe { enabled -> newState { copy(qkReplyEnabled = enabled) } }

        disposables += prefs.qkreplyTapDismiss.asObservable()
                .subscribe { enabled -> newState { copy(qkReplyTapDismiss = enabled) } }
    }

    override fun bindView(view: NotificationPrefsView) {
        super.bindView(view)

        view.preferenceClickIntent
                .autoDisposable(view.scope())
                .subscribe {
                    when (it.id) {
                        R.id.notificationsO -> navigator.showNotificationChannel(threadId)

                        R.id.notifications -> notifications.set(!notifications.get())

                        R.id.previews -> view.showPreviewModeDialog()

                        R.id.vibration -> vibration.set(!vibration.get())

                        R.id.ringtone -> view.showRingtonePicker(ringtone.get().takeIf { it.isNotEmpty() }?.let(Uri::parse))

                        R.id.action1 -> view.showActionDialog(prefs.notifAction1.get())

                        R.id.action2 -> view.showActionDialog(prefs.notifAction2.get())

                        R.id.action3 -> view.showActionDialog(prefs.notifAction3.get())

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

        view.actionsSelectedIntent
                .withLatestFrom(view.preferenceClickIntent) { action, preference ->
                    when (preference.id) {
                        R.id.action1 -> prefs.notifAction1.set(action)
                        R.id.action2 -> prefs.notifAction2.set(action)
                        R.id.action3 -> prefs.notifAction3.set(action)
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()
    }
}