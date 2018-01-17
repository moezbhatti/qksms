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

    companion object {
        private const val MINIMUM_CONTRAST_RATIO = 2
    }

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
            .map { contrastRatio -> contrastRatio < MINIMUM_CONTRAST_RATIO }
            .map { contrastRatio -> if (contrastRatio) R.color.textPrimary else R.color.textPrimaryDark }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val textSecondaryOnTheme: Observable<Int> = prefs.theme.asObservable()
            .map { theme -> measureLuminance(theme) }
            .map { themeLuminance -> secondaryTextLuminance / themeLuminance }
            .map { contrastRatio -> contrastRatio < MINIMUM_CONTRAST_RATIO }
            .map { contrastRatio -> if (contrastRatio) R.color.textSecondary else R.color.textSecondaryDark }
            .map { res -> getColor(res) }
            .distinctUntilChanged()

    val textTertiaryOnTheme: Observable<Int> = prefs.theme.asObservable()
            .map { theme -> measureLuminance(theme) }
            .map { themeLuminance -> tertiaryTextLuminance / themeLuminance }
            .map { contrastRatio -> contrastRatio < MINIMUM_CONTRAST_RATIO }
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
     * Measures the luminance value of a color to be able to measure the contrast ratio between two colors
     * Based on https://stackoverflow.com/a/9733420
     */
    private fun measureLuminance(color: Int): Double {
        val array = intArrayOf(Color.red(color), Color.green(color), Color.blue(color))
                .map { if (it < 0.03928) it / 12.92 else Math.pow((it + 0.055) / 1.055, 2.4) }

        return 0.2126 * array[0] + 0.7152 * array[1] + 0.0722 * array[2] + 0.05
    }

}