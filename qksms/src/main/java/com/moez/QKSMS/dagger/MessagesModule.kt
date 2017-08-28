package com.moez.QKSMS.dagger

import com.moez.QKSMS.data.repository.ConversationRepository
import com.moez.QKSMS.data.repository.MessageRepository
import com.moez.QKSMS.ui.messages.MessageListViewModel
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class MessagesModule(val threadId: Long) {

    @Provides
    @Named("thread_id")
    fun provideThreadId(): Long {
        return threadId
    }

    @ActivityScope
    @Provides
    fun provideRepository(): MessageRepository {
        return MessageRepository(threadId)
    }

    @ActivityScope
    @Provides
    fun provideViewModel(conversations: ConversationRepository, repository: MessageRepository): MessageListViewModel {
        return MessageListViewModel(threadId, conversations, repository)
    }

}