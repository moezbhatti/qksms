package com.moez.QKSMS.presentation.settings

import android.preference.Preference
import com.moez.QKSMS.presentation.base.QkView
import io.reactivex.Observable

interface SettingsView : QkView<SettingsState> {

    val preferencesAddedIntent: Observable<Unit>
    val preferenceClickIntent: Observable<Preference>
    val preferenceChangeIntent: Observable<PreferenceChange>

}

data class PreferenceChange(val preference: Preference, val newValue: Any)