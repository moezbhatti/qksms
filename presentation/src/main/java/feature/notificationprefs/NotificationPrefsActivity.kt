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

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.MenuItemAdapter
import common.base.QkThemedActivity
import common.widget.PreferenceView
import injection.appComponent
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.notification_prefs_activity.*
import kotlinx.android.synthetic.main.settings_switch_widget.view.*
import javax.inject.Inject

class NotificationPrefsActivity : QkThemedActivity<NotificationPrefsViewModel>(), NotificationPrefsView {

    @Inject lateinit var nightModeAdapter: MenuItemAdapter
    @Inject lateinit var mmsSizeAdapter: MenuItemAdapter

    override val viewModelClass = NotificationPrefsViewModel::class
    override val preferenceClickIntent: Subject<PreferenceView> = PublishSubject.create()
    override val ringtoneSelectedIntent: Subject<String> = PublishSubject.create()

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_prefs_activity)
        setTitle(R.string.title_notification_prefs)
        showBackButton(true)
        viewModel.bindView(this)

        nightModeAdapter.setData(R.array.night_modes)
        mmsSizeAdapter.setData(R.array.mms_sizes, R.array.mms_sizes_ids)

        colors.background
                .autoDisposable(scope())
                .subscribe { color -> window.decorView.setBackgroundColor(color) }

        // Listen to clicks for all of the preferences
        (0 until preferences.childCount)
                .map { index -> preferences.getChildAt(index) }
                .filter { view -> view is PreferenceView }
                .forEach { preference ->
                    preference.clicks().subscribe {
                        preferenceClickIntent.onNext(preference as PreferenceView)
                    }
                }
    }

    override fun render(state: NotificationPrefsState) {
        if (state.conversationTitle.isNotEmpty()) {
            title = state.conversationTitle
        }

        notifications.checkbox.isChecked = state.notificationsEnabled
        vibration.checkbox.isChecked = state.vibrationEnabled
        ringtone.summary = state.ringtoneName
    }

    override fun showRingtonePicker(default: Uri) {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, default)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        startActivityForResult(intent, 123)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            uri?.let { ringtoneSelectedIntent.onNext(uri.toString()) }
        }
    }

}