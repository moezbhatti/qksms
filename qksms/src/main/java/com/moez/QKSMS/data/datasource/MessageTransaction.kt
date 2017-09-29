package com.moez.QKSMS.data.datasource

interface MessageTransaction {

    fun markSent(id: Long)

    fun markFailed(id: Long)

    fun markSeen()

    fun markSeen(threadId: Long)

    fun markRead(threadId: Long)

}