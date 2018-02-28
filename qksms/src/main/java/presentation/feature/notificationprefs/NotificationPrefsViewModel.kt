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
import android.net.Uri
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.Preferences
import io.reactivex.rxkotlin.plusAssign
import presentation.common.Navigator
import presentation.common.base.QkViewModel
import timber.log.Timber
import javax.inject.Inject

class NotificationPrefsViewModel(private val intent: Intent) : QkViewModel<NotificationPrefsView, NotificationPrefsState>(NotificationPrefsState()) {

    @Inject lateinit var context: Context
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var prefs: Preferences

    init {
        appComponent.inject(this)

        disposables += prefs.notifications.asObservable()
                .subscribe { enabled -> newState { it.copy(notificationsEnabled = enabled) } }

        disposables += prefs.vibration.asObservable()
                .subscribe { enabled -> newState { it.copy(vibrationEnabled = enabled) } }
    }

    override fun bindView(view: NotificationPrefsView) {
        super.bindView(view)

        view.preferenceClickIntent
                .autoDisposable(view.scope())
                .subscribe {
                    Timber.v("Preference click: ${context.resources.getResourceName(it.id)}")

                    when (it.id) {
                        R.id.notifications -> prefs.notifications.set(!prefs.notifications.get())

                        R.id.vibration -> prefs.vibration.set(!prefs.vibration.get())

                        R.id.ringtone -> view.showRingtonePicker(Uri.parse(prefs.ringtone.get()))
                    }
                }

        view.ringtoneSelectedIntent
                .autoDisposable(view.scope())
                .subscribe { ringtone -> prefs.ringtone.set(ringtone) }
    }
}