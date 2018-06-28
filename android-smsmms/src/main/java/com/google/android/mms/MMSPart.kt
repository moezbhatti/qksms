package com.google.android.mms

class MMSPart @JvmOverloads constructor(
        var name: String = "",
        var mimeType: String = "",
        var data: ByteArray? = null
)