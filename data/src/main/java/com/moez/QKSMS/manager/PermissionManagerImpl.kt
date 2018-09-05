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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.provider.Telephony
import androidx.core.content.ContextCompat
import javax.inject.Inject

class PermissionManagerImpl @Inject constructor(private val context: Context) : PermissionManager {

    override fun isDefaultSms(): Boolean {
        return Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
    }

    override fun hasReadSms(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PERMISSION_GRANTED

    override fun hasSendSms(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PERMISSION_GRANTED

    override fun hasContacts(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PERMISSION_GRANTED

    override fun hasPhone(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PERMISSION_GRANTED

    override fun hasCalling(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PERMISSION_GRANTED

    override fun hasStorage(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED

}