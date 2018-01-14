/*
 * Copyright (C) 2015 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.send_message

import android.app.Activity
import android.content.*
import android.net.Uri
import android.provider.Telephony
import android.util.Log
import com.google.android.mms.MmsException
import com.google.android.mms.util_alt.SqliteWrapper
import java.io.File

open class MmsSentReceiver : BroadcastReceiver() {

    companion object {
        private val TAG = "MmsSentReceiver"

        const val MMS_SENT = "com.klinker.android.messaging.MMS_SENT"
        const val EXTRA_CONTENT_URI = "content_uri"
        const val EXTRA_FILE_PATH = "file_path"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.v(TAG, "MMS sending result: " + resultCode)

        val uri = Uri.parse(intent.getStringExtra(EXTRA_CONTENT_URI))
        Log.v(TAG, uri.toString())

        if (resultCode == Activity.RESULT_OK) {
            Log.v(TAG, "MMS has finished sending, marking it as so in the database")
            val values = ContentValues(1)
            values.put(Telephony.Mms.MESSAGE_BOX, Telephony.Mms.MESSAGE_BOX_SENT)
            SqliteWrapper.update(context, context.contentResolver, uri, values, null, null)
        } else {
            Log.v(TAG, "MMS has failed to send, marking it as so in the database")
            try {
                val messageId = ContentUris.parseId(uri)

                val values = ContentValues(1)
                values.put(Telephony.Mms.MESSAGE_BOX, Telephony.Mms.MESSAGE_BOX_FAILED)
                SqliteWrapper.update(context, context.contentResolver, Telephony.Mms.CONTENT_URI, values,
                        "${Telephony.Mms._ID} = ?", arrayOf(messageId.toString()))

                // TODO this query isn't able to find any results
                // Need to figure out why the message isn't appearing in the PendingMessages Uri,
                // so that we can properly assign the error type
                val errorTypeValues = ContentValues(1)
                errorTypeValues.put(Telephony.MmsSms.PendingMessages.ERROR_TYPE, Telephony.MmsSms.ERR_TYPE_GENERIC_PERMANENT)
                SqliteWrapper.update(context, context.contentResolver, Telephony.MmsSms.PendingMessages.CONTENT_URI,
                        errorTypeValues, "${Telephony.MmsSms.PendingMessages.MSG_ID} = ?", arrayOf(messageId.toString()))

            } catch (e: MmsException) {
                e.printStackTrace()
            }

        }

        val filePath = intent.getStringExtra(EXTRA_FILE_PATH)
        Log.v(TAG, filePath)
        File(filePath).delete()
    }

}
