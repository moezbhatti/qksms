package com.moez.QKSMS.data.sync

import android.net.Uri

internal object ConversationColumns {

    val URI: Uri = Uri.parse("content://mms-sms/conversations?simple=true")
    val PROJECTION = arrayOf(
            android.provider.Telephony.Threads._ID,
            android.provider.Telephony.Threads.DATE,
            android.provider.Telephony.Threads.MESSAGE_COUNT,
            android.provider.Telephony.Threads.RECIPIENT_IDS,
            android.provider.Telephony.Threads.SNIPPET,
            android.provider.Telephony.Threads.SNIPPET_CHARSET,
            android.provider.Telephony.Threads.READ,
            android.provider.Telephony.Threads.ERROR,
            android.provider.Telephony.Threads.HAS_ATTACHMENT)

    val ID = 0
    val DATE = 1
    val MESSAGE_COUNT = 2
    val RECIPIENT_IDS = 3
    val SNIPPET = 4
    val SNIPPET_CS = 5
    val READ = 6
    val ERROR = 7
    val HAS_ATTACHMENT = 8
}