package com.moez.QKSMS.presentation.settings

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayoutManager
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.R
import com.moez.QKSMS.common.di.appComponent
import com.moez.QKSMS.common.util.extensions.dpToPx
import com.moez.QKSMS.presentation.common.base.QkActivity
import com.moez.QKSMS.presentation.common.widget.PreferenceView
import io.reactivex.Observable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.settings_activity.*
import kotlinx.android.synthetic.main.settings_switch_widget.view.*

class SettingsActivity : QkActivity<SettingsViewModel>(), SettingsView {

    override val viewModelClass = SettingsViewModel::class
    override val preferenceClickIntent: Subject<PreferenceView> = PublishSubject.create()
    override val themeSelectedIntent: Observable<Int> by lazy { themeAdapter.colorSelected }

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
                    -0x1f0d0f, -0x4d2025, -0x7f343c, -0xb24954,
                    -0xd95966, -0xff6978, -0xff7685, -0xff8695,
                    -0xff96a4, -0xffb2c0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setTitle(R.string.title_settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        viewModel.bindView(this)

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

        split.checkbox.isChecked = state.splitSmsEnabled

        unicode.checkbox.isChecked = state.stripUnicodeEnabled

        mms.checkbox.isChecked = state.mmsEnabled

        mmsSize.summary = state.maxMmsSize
    }

}