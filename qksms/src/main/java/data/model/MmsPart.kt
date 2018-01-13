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
package data.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class MmsPart : RealmObject() {

    @PrimaryKey var id: Long = 0
    var messageId: Long = 0
    var type: String = ""

    var text: String? = null
    var image: String? = null

    fun isSmil() = "application/smil" == type

    fun isImage() = listOf("image/jpeg", "image/bmp", "image/gif", "image/jpg", "image/png").contains(type)

    fun isText() = "text/plain" == type

}