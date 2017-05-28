package com.moez.QKSMS.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Conversation(

        @PrimaryKey var id: Long = 0,
        var date: Long = 0,
        var messageCount: Int = 0,
        var recipientIds: Int = 0,
        var snippet: String = "",
        var snippetCs: String = "",
        var read: Int = 0,
        var error: Int = 0,
        var hasAttachment: Int = 0

) : RealmObject()
