package com.moez.QKSMS.data.datasource

interface MessageTransaction {

    fun markSeen()

    fun markSeen(threadId: Long)

    fun markRead(threadId: Long)

    fun markSent(id: Long)

    fun markFailed(id: Long)

}