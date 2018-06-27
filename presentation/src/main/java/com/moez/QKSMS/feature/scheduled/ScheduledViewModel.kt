package com.moez.QKSMS.feature.scheduled

import com.moez.QKSMS.common.base.QkViewModel
import com.moez.QKSMS.repository.ScheduledMessageRepository
import javax.inject.Inject

class ScheduledViewModel @Inject constructor(
        scheduledMessageRepo: ScheduledMessageRepository
) : QkViewModel<ScheduledView, ScheduledState>(ScheduledState(
        scheduledMessages = scheduledMessageRepo.getScheduledMessages()
)) {

}