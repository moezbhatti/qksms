package com.moez.QKSMS.presentation.settings

import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import com.moez.QKSMS.R
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject

class SettingsFragment : PreferenceFragment() {

    val preferencesAdded: Subject<Unit> = PublishSubject.create()
    val preferenceClicks: Subject<Preference> = PublishSubject.create()
    val preferenceChanges: Subject<PreferenceChange> = PublishSubject.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.settings)

        preferencesAdded.onNext(Unit)

        val clickListener = Preference.OnPreferenceClickListener {
            preferenceClicks.onNext(it)
            true
        }

        val changeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
            preferenceChanges.onNext(PreferenceChange(preference, newValue))
            true
        }

        (0 until preferenceScreen.preferenceCount)
                .map { index -> preferenceScreen.getPreference(index) }
                .forEach { preference ->
                    preference.onPreferenceClickListener = clickListener
                    preference.onPreferenceChangeListener = changeListener
                }
    }

}