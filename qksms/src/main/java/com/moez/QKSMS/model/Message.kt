package com.moez.QKSMS.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Message(

        @PrimaryKey var id: Long = 0,
        var body: String = ""

) : RealmObject()
