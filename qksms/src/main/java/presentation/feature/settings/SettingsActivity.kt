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
import android.support.v7.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import common.di.appComponent
import common.util.extensions.dpToPx
import common.util.extensions.setVisible
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.settings_activity.*
import kotlinx.android.synthetic.main.settings_switch_widget.view.*
import presentation.common.base.QkActivity
import presentation.common.widget.PreferenceView

class SettingsActivity : QkActivity<SettingsViewModel>(), SettingsView {

    override val viewModelClass = SettingsViewModel::class
    override val preferenceClickIntent: Subject<PreferenceView> = PublishSubject.create()
    override val themeSelectedIntent: Observable<Int> by lazy { themeAdapter.colorSelected }
    override val ringtoneSelectedIntent: Subject<String> = PublishSubject.create()

    // TODO remove this
    private val progressDialog by lazy {
        ProgressDialog(this).apply {
            isIndeterminate = true
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    private val themeDialog by lazy {
        AlertDialog.Builder(this)
                .setTitle(R.string.settings_dialog_theme_title)
                .setView(themeRecyclerView)
                .setCancelable(false)
                .create()
    }

    private val themeRecyclerView by lazy {
        RecyclerView(this).apply {
            val padding = 8.dpToPx(context)
            setPadding(padding, padding, padding, padding)
            adapter = themeAdapter
            clipToPadding = false
            clipChildren = false
            layoutManager = FlexboxLayoutManager(this@SettingsActivity)
        }
    }

    private val themeAdapter by lazy {
        ThemeAdapter(this@SettingsActivity).apply {
            data = listOf(
                    -0x21f24, -0x64245, -0x96678, -0xc93a0, -0x17b1c0, -0x1ae3dd, -0x22e6e3, -0x2fe8ea, -0x3bebef, -0x4fedf6,
                    -0x31b14, -0x74430, -0xb704f, -0xf9d6e, -0x13bf86, -0x16e19d, -0x27e4a0, -0x3de7a5, -0x52eba9, -0x77f1b1,
                    -0xc1a0b, -0x1e4119, -0x316c28, -0x459738, -0x54b844, -0x63d850, -0x71db56, -0x84e05e, -0x95e466, -0xb5eb74,
                    -0x12180a, -0x2e3b17, -0x4c6225, -0x6a8a33, -0x81a83e, -0x98c549, -0xa1ca4f, -0xaed258, -0xbad860, -0xcee46e,
                    -0x17150a, -0x3a3517, -0x605726, -0x867935, -0xa39440, -0xc0ae4b, -0xc6b655, -0xcfc061, -0xd7ca6d, -0xe5dc82,
                    -0x181603, -0x2f2601, -0x504001, -0x6e5801, -0x8c7002, -0xa98804, -0xb19311, -0xbaa122, -0xc4af32, -0xd5c94f,
                    -0x1e0a02, -0x4c1a04, -0x7e2b06, -0xb03c09, -0xd6490a, -0xfc560c, -0xfc641b, -0xfd772f, -0xfd8843, -0xfea865,
                    -0x1f0806, -0x4d140e, -0x7f2116, -0xb22f1f, -0xd93926, -0xff432c, -0xff533f, -0xff6859, -0xff7c71, -0xff9f9c,
                    -0x1f0d0f, -0x4d2025, -0x7f343c, -0xb24954, -0xd95966, -0xff6978, -0xff7685, -0xff8695, -0xff96a4, -0xffb2c0,
                    -0x2f0732, -0x5c165c, -0x8d2a8e, -0xbd42bf, -0xd450d5, -0xda64dc, -0xf570f8, -0xf581f9, -0xfa9100, -0xf2acfe,
                    -0xe0717, -0x231238, -0x3a1e5b, -0x512a7f, -0x63339b, -0x743cb6, -0x834cbe, -0x9760c8, -0xaa74d1, -0xcc96e2,
                    -0x60419, -0xf0b3d, -0x191164, -0x23188b, -0x2b1ea9, -0x3223c7, -0x3f35cd, -0x504bd5, -0x6162dc, -0x7d88e9,
                    -0x219, -0x63c, -0xa63, -0xe8a, -0x11a8, -0x14c5, -0x227cb, -0x43fd3, -0x657db, -0xa80e9,
                    -0x71f, -0x134d, -0x1f7e, -0x2ab1, -0x35d8, -0x3ef9, -0x4d00, -0x6000, -0x7100, -0x9100,
                    -0xc20, -0x1f4e, -0x3380, -0x48b3, -0x58da, -0x6800, -0x47400, -0xa8400, -0x109400, -0x19af00,
                    -0x41619, -0x3344, -0x546f, -0x759b, -0x8fbd, -0xa8de, -0xbaee2, -0x19b5e7, -0x27bceb, -0x40c9f4,
                    -0x101417, -0x283338, -0x43555c, -0x5e7781, -0x72919d, -0x86aab8, -0x92b3bf, -0xa2bfc9, -0xb1cbd2,
                    -0x50506, -0xa0a0b, -0x111112, -0x1f1f20, -0x424243, -0x616162, -0x8a8a8b, -0x9e9e9f, -0xbdbdbe, -0xdededf, -0x1000000, -0x1,
                    -0x13100f, -0x13100f, -0x4f413b, -0x6f5b52, -0x876f64, -0x9f8275, -0xab9186, -0xbaa59c, -0xc8b8b1, -0xd9cdc8)
        }
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

        disposables += colors.background
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
        if (themeDialog.isShowing && !state.selectingTheme) themeDialog.dismiss()
        else if (!themeDialog.isShowing && state.selectingTheme) themeDialog.show()

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

        mmsSize.summary = state.maxMmsSize
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