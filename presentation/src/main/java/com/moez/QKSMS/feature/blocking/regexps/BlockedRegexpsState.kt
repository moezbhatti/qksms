package com.moez.QKSMS.feature.blocking.regexps

import com.moez.QKSMS.model.BlockedRegex
import io.realm.RealmResults

data class BlockedRegexpsState(
        val regexps: RealmResults<BlockedRegex>? = null
)