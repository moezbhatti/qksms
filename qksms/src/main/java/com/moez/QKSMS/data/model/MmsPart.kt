package com.moez.QKSMS.data.model

import android.net.Uri

class MmsPart {
    var name = ""
    var mimeType = ""
    var data: ByteArray? = null
    var path: Uri? = null
}