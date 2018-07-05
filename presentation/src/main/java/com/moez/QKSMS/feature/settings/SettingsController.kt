package com.moez.QKSMS.feature.settings

import android.app.ProgressDialog
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bluelinelabs.conductor.RouterTransaction
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.clicks
import com.moez.QKSMS.BuildConfig
import com.moez.QKSMS.R
import com.moez.QKSMS.common.QkDialog
import com.moez.QKSMS.common.base.QkController
import com.moez.QKSMS.common.util.Colors
import com.moez.QKSMS.common.util.extensions.animateLayoutChanges
import com.moez.QKSMS.common.util.extensions.setBackgroundTint
import com.moez.QKSMS.common.util.extensions.setVisible
import com.moez.QKSMS.common.widget.PreferenceView
import com.moez.QKSMS.feature.settings.about.AboutController
import com.moez.QKSMS.injection.appComponent
import com.moez.QKSMS.util.Preferences
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.settings_activity.view.*
import kotlinx.android.synthetic.main.settings_switch_widget.view.*
import kotlinx.android.synthetic.main.settings_theme_widget.view.*
import javax.inject.Inject

class SettingsController : QkController(), SettingsView {

    @Inject lateinit var context: Context
    @Inject lateinit var colors: Colors
    @Inject lateinit var nightModeDialog: QkDialog
    @Inject lateinit var textSizeDialog: QkDialog
    @Inject lateinit var sendDelayDialog: QkDialog
    @Inject lateinit var mmsSizeDialog: QkDialog

    @Inject lateinit var presenter: SettingsPresenter

    override val preferenceClickIntent: Subject<PreferenceView> = PublishSubject.create()
    override val viewQksmsPlusIntent: Subject<Unit> = PublishSubject.create()
    override val nightModeSelectedIntent by lazy { nightModeDialog.adapter.menuItemClicks }
    override val startTimeSelectedIntent: Subject<Pair<Int, Int>> = PublishSubject.create()
    override val endTimeSelectedIntent: Subject<Pair<Int, Int>> = PublishSubject.create()
    override val textSizeSelectedIntent by lazy { textSizeDialog.adapter.menuItemClicks }
    override val sendDelayChangedIntent by lazy { sendDelayDialog.adapter.menuItemClicks }
    override val mmsSizeSelectedIntent: Subject<Int> by lazy { mmsSizeDialog.adapter.menuItemClicks }

    // TODO remove this
    private val progressDialog by lazy {
        ProgressDialog(activity).apply {
            isIndeterminate = true
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }
    }

    init {
        appComponent.inject(this)
        presenter.onCreate(this)
        retainViewMode = RetainViewMode.RETAIN_DETACH

        colors.themeObservable()
                .autoDisposable(scope())
                .subscribe { activity?.recreate() }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.settings_activity, container, false).apply {
            preferences.postDelayed({ preferences.animateLayoutChanges = true }, 100)

            nightModeDialog.adapter.setData(R.array.night_modes)
            textSizeDialog.adapter.setData(R.array.text_sizes)
            sendDelayDialog.adapter.setData(R.array.delayed_sending_labels)
            mmsSizeDialog.adapter.setData(R.array.mms_sizes, R.array.mms_sizes_ids)

            about.summary = context.getString(R.string.settings_version, BuildConfig.VERSION_NAME)

            // Listen to clicks for all of the preferences
            (0 until preferences.childCount)
                    .map { index -> preferences.getChildAt(index) }
                    .mapNotNull { view -> view as? PreferenceView }
                    .map { preference -> preference.clicks().map { preference } }
                    .let { preferences -> Observable.merge(preferences) }
                    .subscribe(preferenceClickIntent::onNext)
        }
    }

    override fun onAttach(view: View) {
        super.onAttach(view)
        presenter.onAttach(this)
        setTitle(R.string.title_settings)
        showBackButton(true)
    }

    override fun render(state: SettingsState) {
        if (progressDialog.isShowing && !state.syncing) progressDialog.dismiss()
        else if (!progressDialog.isShowing && state.syncing) progressDialog.show()

        view?.run {
            themePreview.setBackgroundTint(state.theme)
            night.summary = state.nightModeSummary
            nightModeDialog.adapter.selectedItem = state.nightModeId
            nightStart.setVisible(state.nightModeId == Preferences.NIGHT_MODE_AUTO)
            nightStart.summary = state.nightStart
            nightEnd.setVisible(state.nightModeId == Preferences.NIGHT_MODE_AUTO)
            nightEnd.summary = state.nightEnd

            black.setVisible(state.nightModeId != Preferences.NIGHT_MODE_OFF)
            black.checkbox.isChecked = state.black

            autoEmoji.checkbox.isChecked = state.autoEmojiEnabled

            delayed.summary = state.sendDelaySummary
            sendDelayDialog.adapter.selectedItem = state.sendDelayId

            delivery.checkbox.isChecked = state.deliveryEnabled

            textSize.summary = state.textSizeSummary
            textSizeDialog.adapter.selectedItem = state.textSizeId
            systemFont.checkbox.isChecked = state.systemFontEnabled

            unicode.checkbox.isChecked = state.stripUnicodeEnabled

            mmsSize.summary = state.maxMmsSizeSummary
            mmsSizeDialog.adapter.selectedItem = state.maxMmsSizeId

        }
    }

    override fun showQksmsPlusSnackbar() {
        view?.run {
            Snackbar.make(contentView, R.string.toast_qksms_plus, Snackbar.LENGTH_LONG).run {
                setAction(R.string.button_more) { viewQksmsPlusIntent.onNext(Unit) }
                show()
            }
        }
    }

    // TODO change this to a PopupWindow
    override fun showNightModeDialog() = nightModeDialog.show(activity!!)

    override fun showStartTimePicker(hour: Int, minute: Int) {
        TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { _, newHour, newMinute ->
            startTimeSelectedIntent.onNext(Pair(newHour, newMinute))
        }, hour, minute, DateFormat.is24HourFormat(activity)).show()
    }

    override fun showEndTimePicker(hour: Int, minute: Int) {
        TimePickerDialog(activity, TimePickerDialog.OnTimeSetListener { _, newHour, newMinute ->
            endTimeSelectedIntent.onNext(Pair(newHour, newMinute))
        }, hour, minute, DateFormat.is24HourFormat(activity)).show()
    }

    override fun showTextSizePicker() = textSizeDialog.show(activity!!)

    override fun showDelayDurationDialog() = sendDelayDialog.show(activity!!)

    override fun showMmsSizePicker() = mmsSizeDialog.show(activity!!)

    override fun showAbout() {
        router.pushController(RouterTransaction.with(AboutController())
                .pushChangeHandler(FadeChangeHandler())
                .popChangeHandler(FadeChangeHandler()))
    }

}