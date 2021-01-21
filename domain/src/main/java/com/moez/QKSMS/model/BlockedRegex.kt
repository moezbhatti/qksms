package com.moez.QKSMS.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class BlockedRegex(
        @PrimaryKey var id: Long = 0,

        var regex: String = ""
) : RealmObject()