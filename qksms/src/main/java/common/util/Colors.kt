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
package common.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.ColorRes
import android.view.View
import com.moez.QKSMS.R
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Colors @Inject constructor(private val context: Context, prefs: Preferences) {

    val materialColors = listOf(
            listOf(0xffffebee, 0xffffcdd2, 0xffef9a9a, 0xffe57373, 0xffef5350, 0xfff44336, 0xffe53935, 0xffd32f2f, 0xffc62828, 0xffb71c1c),
            listOf(0xffFCE4EC, 0xffF8BBD0, 0xffF48FB1, 0xffF06292, 0xffEC407A, 0xffE91E63, 0xffD81B60, 0xffC2185B, 0xffAD1457, 0xff880E4F),
            listOf(0xffF3E5F5, 0xffE1BEE7, 0xffCE93D8, 0xffBA68C8, 0xffAB47BC, 0xff9C27B0, 0xff8E24AA, 0xff7B1FA2, 0xff6A1B9A, 0xff4A148C),
            listOf(0xffEDE7F6, 0xffD1C4E9, 0xffB39DDB, 0xff9575CD, 0xff7E57C2, 0xff673AB7, 0xff5E35B1, 0xff512DA8, 0xff4527A0, 0xff311B92),
            listOf(0xffE8EAF6, 0xffC5CAE9, 0xff9FA8DA, 0xff7986CB, 0xff5C6BC0, 0xff3F51B5, 0xff3949AB, 0xff303F9F, 0xff283593, 0xff1A237E),
            listOf(0xffE3F2FD, 0xffBBDEFB, 0xff90CAF9, 0xff64B5F6, 0xff42A5F5, 0xff2196F3, 0xff1E88E5, 0xff1976D2, 0xff1565C0, 0xff0D47A1),
            listOf(0xffE1F5FE, 0xffB3E5FC, 0xff81D4FA, 0xff4FC3F7, 0xff29B6F6, 0xff03A9F4, 0xff039BE5, 0xff0288D1, 0xff0277BD, 0xff01579B),
            listOf(0xffE0F7FA, 0xffB2EBF2, 0xff80DEEA, 0xff4DD0E1, 0xff26C6DA, 0xff00BCD4, 0xff00ACC1, 0xff0097A7, 0xff00838F, 0xff006064),
            listOf(0xffE0F2F1, 0xffB2DFDB, 0xff80CBC4, 0xff4DB6AC, 0xff26A69A, 0xff009688, 0xff00897B, 0xff00796B, 0xff00695C, 0xff004D40),
            listOf(0xffE8F5E9, 0xffC8E6C9, 0xffA5D6A7, 0xff81C784, 0xff66BB6A, 0xff4CAF50, 0xff43A047, 0xff388E3C, 0xff2E7D32, 0xff1B5E20),
            listOf(0xffF1F8E9, 0xffDCEDC8, 0xffC5E1A5, 0xffAED581, 0xff9CCC65, 0xff8BC34A, 0xff7CB342, 0xff689F38, 0xff558B2F, 0xff33691E),
            listOf(0xffF9FBE7, 0xffF0F4C3, 0xffE6EE9C, 0xffDCE775, 0xffD4E157, 0xffCDDC39, 0xffC0CA33, 0xffAFB42B, 0xff9E9D24, 0xff827717),
            listOf(0xffFFFDE7, 0xffFFF9C4, 0xffFFF59D, 0xffFFF176, 0xffFFEE58, 0xffFFEB3B, 0xffFDD835, 0xffFBC02D, 0xffF9A825, 0xffF57F17),
            listOf(0xffFFF8E1, 0xffFFECB3, 0xffFFE082, 0xffFFD54F, 0xffFFCA28, 0xffFFC107, 0xffFFB300, 0xffFFA000, 0xffFF8F00, 0xffFF6F00),
            listOf(0xffFFF3E0, 0xffFFE0B2, 0xffFFCC80, 0xffFFB74D, 0xffFFA726, 0xffFF9800, 0xffFB8C00, 0xffF57C00, 0xffEF6C00, 0xffE65100),
            listOf(0xffFBE9E7, 0xffFFCCBC, 0xffFFAB91, 0xffFF8A65, 0xffFF7043, 0xffFF5722, 0xffF4511E, 0xffE64A19, 0xffD84315, 0xffBF360C),
            listOf(0xffEFEBE9, 0xffD7CCC8, 0xffBCAAA4, 0xffA1887F, 0xff8D6E63, 0xff795548, 0xff6D4C41, 0xff5D4037, 0xff4E342E, 0xff3E2723),
            listOf(0xffFAFAFA, 0xffF5F5F5, 0xffEEEEEE, 0xffE0E0E0, 0xffBDBDBD, 0xff9E9E9E, 0xff757575, 0xff616161, 0xff424242, 0xff212121),
            listOf(0xffECEFF1, 0xffCFD8DC, 0xffB0BEC5, 0xff90A4AE, 0xff78909C, 0xff607D8B, 0xff546E7A, 0xff455A64, 0xff37474F, 0xff263238))
            .map { it.map { it.toInt() } }

    private val minimumContrastRatio = 2

    // Cache these values so they don't need to be recalculated
    private val primaryTextLuminance = measureLuminance(getColor(R.color.textPrimaryDark))
    private val secondaryTextLuminance = measureLuminance(getColor(R.color.textSecondaryDark))
    private val tertiaryTextLuminance = measureLuminance(getColor(R.color.textTertiaryDark))

    val theme: Observable<Int> = prefs.theme.asObservable()
            .distinctUntilChanged()

    val appThemeResources: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.style.AppThemeDark else R.style.AppThemeLight }

    val popupThemeResource: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.style.PopupThemeDark else R.style.PopupThemeLight }

    @SuppressLint("InlinedApi")
    val statusBarIcons: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> dark || Build.VERSION.SDK_INT < Build.VERSION_CODES.M }
            .map { lightIcons -> if (lightIcons) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR }
            .distinctUntilChanged()

    val statusBar: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.statusBarDark else R.color.statusBarLight }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val toolbarColor: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.toolbarDark else R.color.toolbarLight }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val background: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.backgroundDark else R.color.white }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val field: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.fieldDark else R.color.fieldLight }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val composeBackground: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.backgroundDark else R.color.backgroundLight }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val separator: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.separatorDark else R.color.separatorLight }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val textPrimary: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.textPrimaryDark else R.color.textPrimary }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val textSecondary: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.textSecondaryDark else R.color.textSecondary }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val textTertiary: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.textTertiaryDark else R.color.textTertiary }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val textPrimaryOnTheme: Observable<Int> = prefs.theme.asObservable()
            .map { theme -> measureLuminance(theme) }
            .map { themeLuminance -> primaryTextLuminance / themeLuminance }
            .map { contrastRatio -> contrastRatio < minimumContrastRatio }
            .map { contrastRatio -> if (contrastRatio) R.color.textPrimary else R.color.textPrimaryDark }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val textSecondaryOnTheme: Observable<Int> = prefs.theme.asObservable()
            .map { theme -> measureLuminance(theme) }
            .map { themeLuminance -> secondaryTextLuminance / themeLuminance }
            .map { contrastRatio -> contrastRatio < minimumContrastRatio }
            .map { contrastRatio -> if (contrastRatio) R.color.textSecondary else R.color.textSecondaryDark }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val textTertiaryOnTheme: Observable<Int> = prefs.theme.asObservable()
            .map { theme -> measureLuminance(theme) }
            .map { themeLuminance -> tertiaryTextLuminance / themeLuminance }
            .map { contrastRatio -> contrastRatio < minimumContrastRatio }
            .map { contrastRatio -> if (contrastRatio) R.color.textTertiary else R.color.textTertiaryDark }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val bubble: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.bubbleDark else R.color.bubbleLight }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val switchThumbEnabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.switchThumbEnabledDark else R.color.switchThumbEnabledLight }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val switchThumbDisabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.switchThumbDisabledDark else R.color.switchThumbDisabledLight }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val switchTrackEnabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.switchTrackEnabledDark else R.color.switchTrackEnabledLight }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val switchTrackDisabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.switchTrackDisabledDark else R.color.switchTrackDisabledLight }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    private fun getColor(@ColorRes res: Int): Int {
        return context.resources.getColor(res)
    }

    /**
     * Measures the luminance value of a color to be able to measure the contrast ratio between two materialColors
     * Based on https://stackoverflow.com/a/9733420
     */
    private fun measureLuminance(color: Int): Double {
        val array = intArrayOf(Color.red(color), Color.green(color), Color.blue(color))
                .map { if (it < 0.03928) it / 12.92 else Math.pow((it + 0.055) / 1.055, 2.4) }

        return 0.2126 * array[0] + 0.7152 * array[1] + 0.0722 * array[2] + 0.05
    }

}