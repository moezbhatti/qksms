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
package com.moez.QKSMS.feature.plus.experiment

import android.content.Context
import androidx.annotation.StringRes
import com.moez.QKSMS.R
import com.moez.QKSMS.experiment.Experiment
import com.moez.QKSMS.experiment.Variant
import com.moez.QKSMS.manager.AnalyticsManager
import javax.inject.Inject

class UpgradeButtonExperiment @Inject constructor(
    context: Context,
    analytics: AnalyticsManager
) : Experiment<Int>(context, analytics) {

    override val key: String = "Upgrade Button"

    override val variants: List<Variant<Int>> = listOf(
            Variant("variant_a", R.string.qksms_plus_upgrade),
            Variant("variant_b", R.string.qksms_plus_upgrade_b),
            Variant("variant_c", R.string.qksms_plus_upgrade_c),
            Variant("variant_d", R.string.qksms_plus_upgrade_d))

    override val default: Int = R.string.qksms_plus_upgrade

}