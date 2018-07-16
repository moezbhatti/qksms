package com.moez.QKSMS.feature.scheduled

import com.moez.QKSMS.model.ScheduledMessage
import io.realm.RealmResults

data class ScheduledState(
        val scheduledMessages: RealmResults<ScheduledMessage>? = null,
        val upgraded: Boolean = false
)
