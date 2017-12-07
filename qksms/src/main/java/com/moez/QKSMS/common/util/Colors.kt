package com.moez.QKSMS.common.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import com.moez.QKSMS.R
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Colors @Inject constructor(context: Context, prefs: Preferences) {

    companion object {
        private val MINIMUM_CONTRAST_RATIO = 2
    }

    // Cache these values so they don't need to be recalculated
    private val primaryTextLuminance = measureLuminance(context.resources.getColor(R.color.textPrimaryDark))
    private val secondaryTextLuminance = measureLuminance(context.resources.getColor(R.color.textSecondaryDark))
    private val tertiaryTextLuminance = measureLuminance(context.resources.getColor(R.color.textTertiaryDark))

    val theme: Observable<Int> = prefs.theme.asObservable()
            .distinctUntilChanged()

    @SuppressLint("InlinedApi")
    val statusBarIcons: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> dark || Build.VERSION.SDK_INT < Build.VERSION_CODES.M }
            .map { lightIcons -> if (lightIcons) 0 else View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR }
            .distinctUntilChanged()

    val statusBar: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.statusBarDark else R.color.statusBarLight }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val toolbarColor: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.toolbarDark else R.color.toolbarLight }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val background: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.backgroundDark else R.color.white }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val field: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.fieldDark else R.color.fieldLight }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val composeBackground: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.backgroundDark else R.color.backgroundLight }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val separator: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.separatorDark else R.color.separatorLight }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val textPrimary: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.textPrimaryDark else R.color.textPrimary }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val textSecondary: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.textSecondaryDark else R.color.textSecondary }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val textTertiary: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.textTertiaryDark else R.color.textTertiary }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val textPrimaryOnTheme: Observable<Int> = prefs.theme.asObservable()
            .map { theme -> measureLuminance(theme) }
            .map { themeLuminance -> primaryTextLuminance / themeLuminance }
            .map { contrastRatio -> contrastRatio < MINIMUM_CONTRAST_RATIO }
            .map { contrastRatio -> if (contrastRatio) R.color.textPrimary else R.color.textPrimaryDark }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val textSecondaryOnTheme: Observable<Int> = prefs.theme.asObservable()
            .map { theme -> measureLuminance(theme) }
            .map { themeLuminance -> secondaryTextLuminance / themeLuminance }
            .map { contrastRatio -> contrastRatio < MINIMUM_CONTRAST_RATIO }
            .map { contrastRatio -> if (contrastRatio) R.color.textSecondary else R.color.textSecondaryDark }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val textTertiaryOnTheme: Observable<Int> = prefs.theme.asObservable()
            .map { theme -> measureLuminance(theme) }
            .map { themeLuminance -> tertiaryTextLuminance / themeLuminance }
            .map { contrastRatio -> contrastRatio < MINIMUM_CONTRAST_RATIO }
            .map { contrastRatio -> if (contrastRatio) R.color.textTertiary else R.color.textTertiaryDark }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val bubble: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.bubbleDark else R.color.bubbleLight }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val switchThumbEnabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.switchThumbEnabledDark else R.color.switchThumbEnabledLight }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val switchThumbDisabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.switchThumbDisabledDark else R.color.switchThumbDisabledLight }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val switchTrackEnabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.switchTrackEnabledDark else R.color.switchTrackEnabledLight }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

    val switchTrackDisabled: Observable<Int> = prefs.dark.asObservable()
            .map { dark -> if (dark) R.color.switchTrackDisabledDark else R.color.switchTrackDisabledLight }
            .map { res -> context.resources.getColor(res) }
            .distinctUntilChanged()

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