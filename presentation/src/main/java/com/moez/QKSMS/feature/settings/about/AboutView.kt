package com.moez.QKSMS.feature.settings.about

import com.moez.QKSMS.common.base.QkView
import com.moez.QKSMS.common.widget.PreferenceView
import io.reactivex.subjects.Subject

interface AboutView : QkView<Unit> {

    val preferenceClickIntent: Subject<PreferenceView>

}