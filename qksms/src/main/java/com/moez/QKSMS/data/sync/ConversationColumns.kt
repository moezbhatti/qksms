package com.moez.QKSMS.data.sync

import android.net.Uri

internal object ConversationColumns {

    val URI: Uri = Uri.parse("content://mms-sms/conversations?simple=true")
    val PROJECTION = arrayOf(
            android.provider.Telephony.Threads._ID,
            android.provider.Telephony.Threads.RECIPIENT_IDS)

    val ID = 0
    val RECIPIENT_IDS = 1
}