package com.moez.QKSMS.util

import android.net.Uri
import com.f2prateek.rx.preferences2.Preference.Converter

class UriPreferenceConverter : Converter<Uri> {

    override fun deserialize(serialized: String): Uri {
        return Uri.parse(serialized)
    }

    override fun serialize(value: Uri): String {
        return value.toString()
    }

}
