package com.moez.QKSMS.presentation.settings

import com.moez.QKSMS.presentation.base.QkView
import com.moez.QKSMS.presentation.view.PreferenceView
import io.reactivex.Observable
import io.reactivex.subjects.Subject

interface SettingsView : QkView<SettingsState> {
    val preferenceClickIntent: Subject<PreferenceView>
    val themeSelectedIntent: Observable<Int>
}
