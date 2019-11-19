/*
 * Copyright (C) 2019 Moez Bhatti <moez.bhatti@gmail.com>
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
package com.moez.QKSMS.repository

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import com.moez.QKSMS.util.PhoneNumberUtils
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class PhoneNumberUtilsTest {

    private val context = InstrumentationRegistry.getInstrumentation().context
    private val phoneNumberUtils = PhoneNumberUtils(context)

    @Before
    fun setup() {
        Locale.setDefault(Locale.US)
    }

    @Test
    fun compare_identicalNumbers_returnsTrue() {
        assertTrue(phoneNumberUtils.compare("+1 123 456 7890", "+1 123 456 7890"))
    }

    @Test
    fun compare_IdenticalNsnsWithOneMissingCountryCode_returnsTrue() {
        assertTrue(phoneNumberUtils.compare("+1 123 456 7890", "123 456 7890"))
    }

    @Test
    fun compare_IdenticalNsnsWithOnePoorlyFormattedCountryCode_returnsTrue() {
        assertTrue(phoneNumberUtils.compare("+1 123 456 7890", "1 123 456 7890"))
    }

    @Test
    fun compare_IdenticalFullNationalAustralianNsnsWithOneMissingCountryCode_returnsTrue() {
        assertTrue(phoneNumberUtils.compare("+61 4 1234 5678", "04 1234 5678"))
    }

    @Test
    fun compare_InvalidShortNsnMatch_returnsFalse() {
        assertFalse(phoneNumberUtils.compare("+1 123 456 7890", "67890"))
    }

    @Test
    fun compare_unequalNumbers_returnsFalse() {
        assertFalse(phoneNumberUtils.compare("123 456 7890", "234 567 8901"))
    }

}
