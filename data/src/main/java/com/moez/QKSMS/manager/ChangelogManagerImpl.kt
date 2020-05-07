/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.manager

import android.content.Context
import com.moez.QKSMS.common.util.extensions.versionCode
import com.moez.QKSMS.util.Preferences
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject

class ChangelogManagerImpl @Inject constructor(
    private val context: Context,
    private val moshi: Moshi,
    private val prefs: Preferences
) : ChangelogManager {

    override fun didUpdate(): Boolean = prefs.changelogVersion.get() != context.versionCode

    override fun getChangelog(): Single<ChangelogManager.Changelog> {
        val url = "https://firestore.googleapis.com/v1/projects/qksms-app/databases/(default)/documents/changelog"
        val request = url.toHttpUrlOrNull()?.let { Request.Builder().url(it).build() }
        val call = request?.let { OkHttpClient().newCall(it) }
        val adapter = moshi.adapter(ChangelogResponse::class.java)

        return Single
                .create<Response> { emitter ->
                    call?.enqueue(object : Callback {
                        override fun onResponse(call: Call, response: Response) {
                            if (!emitter.isDisposed) {
                                emitter.onSuccess(response)
                            }
                        }

                        override fun onFailure(call: Call, e: IOException) {
                            if (!emitter.isDisposed) {
                                emitter.onError(e)
                            }
                        }
                    })
                    emitter.setCancellable {
                        call?.cancel()
                    }
                }
                .map { response -> response.body?.string()?.let(adapter::fromJson) }
                .map { response ->
                    response.documents
                            .sortedBy { document -> document.fields.versionCode.value }
                            .filter { document ->
                                val range = (prefs.changelogVersion.get() + 1)..context.versionCode
                                document.fields.versionCode.value.toInt() in range
                            }
                }
                .map { documents ->
                    val added = documents.fold(listOf<String>()) { acc, document ->
                        acc + document.fields.added?.value?.values?.map { value -> value.value }.orEmpty()
                    }
                    val improved = documents.fold(listOf<String>()) { acc, document ->
                        acc + document.fields.improved?.value?.values?.map { value -> value.value }.orEmpty()
                    }
                    val fixed = documents.fold(listOf<String>()) { acc, document ->
                        acc + document.fields.fixed?.value?.values?.map { value -> value.value }.orEmpty()
                    }
                    ChangelogManager.Changelog(added, improved, fixed)
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    override fun markChangelogSeen() {
        prefs.changelogVersion.set(context.versionCode)
    }

    @JsonClass(generateAdapter = true)
    data class ChangelogResponse(
        @Json(name = "documents") val documents: List<Document>
    )

    @JsonClass(generateAdapter = true)
    data class Document(
        @Json(name = "fields") val fields: Changelog
    )

    @JsonClass(generateAdapter = true)
    data class Changelog(
        @Json(name = "added") val added: ArrayField?,
        @Json(name = "improved") val improved: ArrayField?,
        @Json(name = "fixed") val fixed: ArrayField?,
        @Json(name = "versionName") val versionName: StringField,
        @Json(name = "versionCode") val versionCode: IntegerField
    )

    @JsonClass(generateAdapter = true)
    data class ArrayField(
        @Json(name = "arrayValue") val value: ArrayValues
    )

    @JsonClass(generateAdapter = true)
    data class ArrayValues(
        @Json(name = "values") val values: List<StringField>
    )

    @JsonClass(generateAdapter = true)
    data class StringField(
        @Json(name = "stringValue") val value: String
    )

    @JsonClass(generateAdapter = true)
    data class IntegerField(
        @Json(name = "integerValue") val value: String
    )

}
