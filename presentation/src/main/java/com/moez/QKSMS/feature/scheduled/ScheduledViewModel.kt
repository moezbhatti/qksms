package com.moez.QKSMS.feature.scheduled

import com.moez.QKSMS.common.Navigator
import com.moez.QKSMS.common.androidxcompat.scope
import com.moez.QKSMS.common.base.QkViewModel
import com.moez.QKSMS.common.util.BillingManager
import com.moez.QKSMS.interactor.SendScheduledMessage
import com.moez.QKSMS.repository.ScheduledMessageRepository
import com.uber.autodispose.kotlin.autoDisposable
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.rxkotlin.withLatestFrom
import javax.inject.Inject

class ScheduledViewModel @Inject constructor(
        billingManager: BillingManager,
        private val navigator: Navigator,
        private val scheduledMessageRepo: ScheduledMessageRepository,
        private val sendScheduledMessage: SendScheduledMessage
) : QkViewModel<ScheduledView, ScheduledState>(ScheduledState(
        scheduledMessages = scheduledMessageRepo.getScheduledMessages()
)) {

    init {
        disposables += billingManager.upgradeStatus
                .subscribe { upgraded -> newState { copy(upgraded = upgraded) } }
    }

    override fun bindView(view: ScheduledView) {
        super.bindView(view)

        view.messageClickIntent
                .autoDisposable(view.scope())
                .subscribe { view.showMessageOptions() }

        view.messageMenuIntent
                .withLatestFrom(view.messageClickIntent) { itemId, messageId ->
                    when (itemId) {
                        0 -> sendScheduledMessage.execute(messageId)
                        1 -> scheduledMessageRepo.deleteScheduledMessage(messageId)
                    }
                }
                .autoDisposable(view.scope())
                .subscribe()

        view.upgradeIntent
                .autoDisposable(view.scope())
                .subscribe { navigator.showQksmsPlusActivity("schedule_fab") }
    }

}