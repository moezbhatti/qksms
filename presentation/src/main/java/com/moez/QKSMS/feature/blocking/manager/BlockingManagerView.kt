package com.moez.QKSMS.feature.blocking.manager

import com.moez.QKSMS.common.base.QkViewContract
import io.reactivex.Observable

interface BlockingManagerView : QkViewContract<BlockingManagerState> {

    fun qksmsClicked(): Observable<*>
    fun callControlClicked(): Observable<*>
    fun siaClicked(): Observable<*>

}
