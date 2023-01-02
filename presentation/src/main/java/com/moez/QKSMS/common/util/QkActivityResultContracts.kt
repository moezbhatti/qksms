package com.moez.QKSMS.common.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import androidx.activity.result.contract.ActivityResultContract

class QkActivityResultContracts {

    data class OpenDocumentParams(
        val mimeTypes: List<String>,
        val initialUri: Uri? = null
    )

    class OpenDocument : ActivityResultContract<OpenDocumentParams, Uri>() {
        override fun createIntent(context: Context, input: OpenDocumentParams): Intent {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .putExtra(Intent.EXTRA_MIME_TYPES, input.mimeTypes.toTypedArray())
                    .setType("*/*")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, input.initialUri)
            }

            return intent
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            if (intent == null || resultCode != Activity.RESULT_OK) {
                return null
            }

            return intent.data
        }
    }

}
