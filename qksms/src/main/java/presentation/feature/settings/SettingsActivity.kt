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
package presentation.feature.settings

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.di.appComponent
import common.util.extensions.dpToPx
import common.util.extensions.setVisible
import data.model.MenuItem
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.settings_activity.*
import kotlinx.android.synthetic.main.settings_switch_widget.view.*
import presentation.common.MenuItemAdapter
import presentation.common.base.QkActivity
import presentation.common.widget.PreferenceView
import javax.inject.Inject

class SettingsActivity : QkActivity<SettingsViewModel>(), SettingsView {

    @Inject lateinit var menuItemAdapter: MenuItemAdapter

    override val viewModelClass = SettingsViewModel::class
    override val preferenceClickIntent: Subject<PreferenceView> = PublishSubject.create()
    override val ringtoneSelectedIntent: Subject<String> = PublishSubject.create()
    override val mmsSizeSelectedIntent: Subject<Int> by lazy { menuItemAdapter.menuItemClicks }

    // TODO remove this
    private val progressDialog by lazy {
        ProgressDialog(this).apply {
            isIndeterminate = true
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    private val mmsSizeDialog by lazy {
        menuItemAdapter.data = arrayListOf(
                MenuItem(R.string.menu_mms_size_100kb, 100),
                MenuItem(R.string.menu_mms_size_200kb, 200),
                MenuItem(R.string.menu_mms_size_300kb, 300),
                MenuItem(R.string.menu_mms_size_600kb, 600),
                MenuItem(R.string.menu_mms_size_1000kb, 1000),
                MenuItem(R.string.menu_mms_size_no_limit, 0))

        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = menuItemAdapter
        recyclerView.setPadding(0, 8.dpToPx(this), 0, 8.dpToPx(this))
        AlertDialog.Builder(this)
                .setTitle(R.string.settings_mms_size_title)
                .setMessage(R.string.settings_mms_size_summary)
                .setNegativeButton(R.string.button_cancel, null)
                .setView(recyclerView)
                .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setTitle(R.string.title_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.bindView(this)

        val supportsNotificationChannels = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        notificationsO.setVisible(supportsNotificationChannels)
        notifications.setVisible(!supportsNotificationChannels)
        vibration.setVisible(!supportsNotificationChannels)
        ringtone.setVisible(!supportsNotificationChannels)

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

    override fun render(state: SettingsState) {
        if (progressDialog.isShowing && !state.syncing) progressDialog.dismiss()
        else if (!progressDialog.isShowing && state.syncing) progressDialog.show()

        defaultSms.summary = getString(when (state.isDefaultSmsApp) {
            true -> R.string.settings_default_sms_summary_true
            else -> R.string.settings_default_sms_summary_false
        })

        dark.checkbox.isChecked = state.darkModeEnabled

        autoEmoji.checkbox.isChecked = state.autoEmojiEnabled

        notifications.checkbox.isChecked = state.notificationsEnabled

        vibration.checkbox.isChecked = state.vibrationEnabled

        delivery.checkbox.isChecked = state.deliveryEnabled

        unicode.checkbox.isChecked = state.stripUnicodeEnabled

        mms.checkbox.isChecked = state.mmsEnabled

        menuItemAdapter.selectedItem = state.maxMmsSize
    }

    override fun showRingtonePicker(default: Uri) {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, default)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
        startActivityForResult(intent, 123)
    }

    override fun showMmsSizePicker() = mmsSizeDialog.show()

    override fun dismissMmsSizePicker() = mmsSizeDialog.dismiss()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            uri?.let { ringtoneSelectedIntent.onNext(uri.toString()) }
        }
    }

}