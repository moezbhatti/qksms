package com.moez.QKSMS.feature.scheduled

import com.moez.QKSMS.common.base.QkView
import io.reactivex.Observable

interface ScheduledView : QkView<ScheduledState> {

    val messageClickIntent: Observable<Long>
    val messageMenuIntent: Observable<Int>
    val upgradeIntent: Observable<*>

    fun showMessageOptions()

}
