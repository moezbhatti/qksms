package com.moez.QKSMS.repository

import android.telephony.SmsMessage

interface AndroidMessagesRepository {
    fun deleteMessage(messages: Array<SmsMessage>)
}