package com.moez.QKSMS.feature.blocking.regexps

import com.moez.QKSMS.common.base.QkViewContract
import io.reactivex.Observable

interface BlockedRegexpsView : QkViewContract<BlockedRegexpsState> {

    fun unblockRegex(): Observable<Long>
    fun addRegex(): Observable<*>
    fun saveRegex(): Observable<String>

    fun showAddDialog()

}
