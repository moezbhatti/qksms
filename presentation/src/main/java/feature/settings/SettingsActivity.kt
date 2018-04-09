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
package feature.settings

import android.app.AlertDialog
import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.kotlin.autoDisposable
import common.MenuItemAdapter
import common.base.QkThemedActivity
import common.util.extensions.dpToPx
import common.util.extensions.setBackgroundTint
import common.util.extensions.setVisible
import common.widget.PreferenceView
import injection.appComponent
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.settings_activity.*
import kotlinx.android.synthetic.main.settings_switch_widget.view.*
import kotlinx.android.synthetic.main.settings_theme_widget.*
import util.Preferences
import javax.inject.Inject

class SettingsActivity : QkThemedActivity<SettingsViewModel>(), SettingsView {

    @Inject lateinit var nightModeAdapter: MenuItemAdapter
    @Inject lateinit var textSizeAdapter: MenuItemAdapter
    @Inject lateinit var mmsSizeAdapter: MenuItemAdapter

    override val viewModelClass = SettingsViewModel::class
    override val preferenceClickIntent: Subject<PreferenceView> = PublishSubject.create()
    override val nightModeSelectedIntent by lazy { nightModeAdapter.menuItemClicks }
    override val viewQksmsPlusIntent: Subject<Unit> = PublishSubject.create()
    override val startTimeSelectedIntent: Subject<Pair<Int, Int>> = PublishSubject.create()
    override val endTimeSelectedIntent: Subject<Pair<Int, Int>> = PublishSubject.create()
    override val textSizeSelectedIntent by lazy { textSizeAdapter.menuItemClicks }
    override val mmsSizeSelectedIntent: Subject<Int> by lazy { mmsSizeAdapter.menuItemClicks }

    // TODO remove this
    private val progressDialog by lazy {
        ProgressDialog(this).apply {
            isIndeterminate = true
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    private var nightModeDialog: AlertDialog? = null
    private var textSizeDialog: AlertDialog? = null
    private var mmsSizeDialog: AlertDialog? = null

    init {
        appComponent.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setTitle(R.string.title_settings)
        showBackButton(true)
        viewModel.bindView(this)

        nightModeAdapter.setData(R.array.night_modes)
        textSizeAdapter.setData(R.array.text_sizes)
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

    override fun render(state: SettingsState) {
        if (progressDialog.isShowing && !state.syncing) progressDialog.dismiss()
        else if (!progressDialog.isShowing && state.syncing) progressDialog.show()

        defaultSms.summary = getString(when (state.isDefaultSmsApp) {
            true -> R.string.settings_default_sms_summary_true
            else -> R.string.settings_default_sms_summary_false
        })

        themePreview.setBackgroundTint(state.theme)
        night.summary = state.nightModeSummary
        nightStart.setVisible(state.nightModeId == Preferences.NIGHT_MODE_AUTO)
        nightStart.summary = state.nightStart
        nightEnd.setVisible(state.nightModeId == Preferences.NIGHT_MODE_AUTO)
        nightEnd.summary = state.nightEnd

        black.setVisible(state.nightModeId != Preferences.NIGHT_MODE_OFF)
        black.checkbox.isChecked = state.black

        autoEmoji.checkbox.isChecked = state.autoEmojiEnabled
        delivery.checkbox.isChecked = state.deliveryEnabled
        qkreply.checkbox.isChecked = state.qkReplyEnabled
        qkreplyTapDismiss.setVisible(state.qkReplyEnabled)
        qkreplyTapDismiss.checkbox.isChecked = state.qkReplyTapDismiss

        textSize.summary = state.textSizeSummary

        unicode.checkbox.isChecked = state.stripUnicodeEnabled

        mmsSize.summary = state.maxMmsSizeSummary
        mmsSizeAdapter.selectedItem = state.maxMmsSizeId
    }

    // TODO change this to a PopupWindow
    override fun showNightModeDialog() {
        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = nightModeAdapter
        recyclerView.setPadding(0, 8.dpToPx(this), 0, 8.dpToPx(this))
        nightModeDialog = AlertDialog.Builder(this)
                .setTitle(R.string.settings_night_title)
                .setView(recyclerView)
                .create().apply { show() }
    }

    override fun dismissNightModeDialog() {
        nightModeDialog?.dismiss()
        nightModeDialog = null
    }

    override fun showQksmsPlusSnackbar() {
        Snackbar.make(contentView, R.string.toast_qksms_plus, Snackbar.LENGTH_LONG).run {
            setAction(R.string.button_more, { viewQksmsPlusIntent.onNext(Unit) })
            show()
        }
    }

    override fun showStartTimePicker(hour: Int, minute: Int) {
        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, newHour, newMinute ->
            startTimeSelectedIntent.onNext(Pair(newHour, newMinute))
        }, hour, minute, false).show()
    }

    override fun showEndTimePicker(hour: Int, minute: Int) {
        TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { _, newHour, newMinute ->
            endTimeSelectedIntent.onNext(Pair(newHour, newMinute))
        }, hour, minute, false).show()
    }

    override fun showTextSizePicker() {
        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = textSizeAdapter
        recyclerView.setPadding(0, 8.dpToPx(this), 0, 8.dpToPx(this))
        textSizeDialog = AlertDialog.Builder(this)
                .setTitle(R.string.settings_text_size_title)
                .setView(recyclerView)
                .create().apply { show() }
    }

    override fun dismissTextSizePicker() {
        textSizeDialog?.dismiss()
        textSizeDialog = null
    }

    override fun showMmsSizePicker() {
        val recyclerView = RecyclerView(this)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mmsSizeAdapter
        recyclerView.setPadding(0, 8.dpToPx(this), 0, 8.dpToPx(this))
        mmsSizeDialog = AlertDialog.Builder(this)
                .setTitle(R.string.settings_mms_size_title)
                .setNegativeButton(R.string.button_cancel, null)
                .setView(recyclerView)
                .create().apply { show() }
    }

    override fun dismissMmsSizePicker() {
        mmsSizeDialog?.dismiss()
        mmsSizeDialog = null
    }

}