package com.moez.QKSMS.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.AnalyticsManager;
import com.moez.QKSMS.common.CIELChEvaluator;
import com.moez.QKSMS.common.ConversationPrefsHelper;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.common.utils.ColorUtils;
import com.moez.QKSMS.receiver.IconColorReceiver;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.dialog.QKDialog;
import com.moez.QKSMS.ui.settings.SettingsFragment;
import com.moez.QKSMS.ui.view.QKTextView;
import com.moez.QKSMS.ui.view.colorpicker.ColorPickerPalette;
import com.moez.QKSMS.ui.widget.WidgetProvider;

public class ThemeManager {
    private final static String TAG = "ThemeManager";

    public static final int DEFAULT_COLOR = 0xff009688;
    public static final int TRANSITION_LENGTH = 500;

    public enum Theme {
        LIGHT,
        DARK,
        BLACK;

        public static final String PREF_OFFWHITE = "light";
        public static final String PREF_GREY = "grey";
        public static final String PREF_BLACK = "black";

        public static Theme fromString(String color) {
            switch (color) {
                case PREF_OFFWHITE:
                    return LIGHT;
                case PREF_GREY:
                    return DARK;
                case PREF_BLACK:
                    return BLACK;
                default:
                    Log.w(TAG, "Tried to set theme with invalid string: " + color);
                    return LIGHT;
            }
        }
    }

    // Colors copied from http://www.google.com/design/spec/style/color.html#color-ui-color-palette
    private static final int[][] COLORS = {{
            // Red
            0xfffde0dc, 0xfff9bdbb, 0xfff69988, 0xfff36c60,
            0xffe84e40, 0xffe51c23, 0xffdd191d, 0xffd01716,
            0xffc41411, 0xffb0120a
    }, {    // Pink
            0xfffce4ec, 0xfff8bbd0, 0xfff48fb1, 0xfff06292,
            0xffec407a, 0xffe91e63, 0xffd81b60, 0xffc2185b,
            0xffad1457, 0xff880e4f
    }, {    // Purple
            0xfff3e5f5, 0xffe1bee7, 0xffce93d8, 0xffba68c8,
            0xffab47bc, 0xff9c27b0, 0xff8e24aa, 0xff7b1fa2,
            0xff6a1b9a, 0xff4a148c
    }, {    // Deep Purple
            0xffede7f6, 0xffd1c4e9, 0xffb39ddb, 0xff9575cd,
            0xff7e57c2, 0xff673ab7, 0xff5e35b1, 0xff512da8,
            0xff4527a0, 0xff311b92
    }, {    // Indigo
            0xffe8eaf6, 0xffc5cae9, 0xff9fa8da, 0xff7986cb,
            0xff5c6bc0, 0xff3f51b5, 0xff3949ab, 0xff303f9f,
            0xff283593, 0xff1a237e
    }, {    // Blue
            0xffe7e9fd, 0xffd0d9ff, 0xffafbfff, 0xff91a7ff,
            0xff738ffe, 0xff5677fc, 0xff4e6cef, 0xff455ede,
            0xff3b50ce, 0xff2a36b1
    }, {    // Light Blue
            0xffe1f5fe, 0xffb3e5fc, 0xff81d4fa, 0xff4fc3f7,
            0xff29b6f6, 0xff03a9f4, 0xff039be5, 0xff0288d1,
            0xff0277bd, 0xff01579b
    }, {    // Cyan
            0xffe0f7fa, 0xffb2ebf2, 0xff80deea, 0xff4dd0e1,
            0xff26c6da, 0xff00bcd4, 0xff00acc1, 0xff0097a7,
            0xff00838f, 0xff006064
    }, {    // Teal
            0xffe0f2f1, 0xffb2dfdb, 0xff80cbc4, 0xff4db6ac,
            0xff26a69a, 0xff009688, 0xff00897b, 0xff00796b,
            0xff00695c, 0xff004d40
    }, {    // Green
            0xffd0f8ce, 0xffa3e9a4, 0xff72d572, 0xff42bd41,
            0xff2baf2b, 0xff259b24, 0xff0a8f08, 0xff0a7e07,
            0xff056f00, 0xff0d5302
    }, {    // Light Green
            0xfff1f8e9, 0xffdcedc8, 0xffc5e1a5, 0xffaed581,
            0xff9ccc65, 0xff8bc34a, 0xff7cb342, 0xff689f38,
            0xff558b2f, 0xff33691e
    }, {    // Lime
            0xfff9fbe7, 0xfff0f4c3, 0xffe6ee9c, 0xffdce775,
            0xffd4e157, 0xffcddc39, 0xffc0ca33, 0xffafb42b,
            0xff9e9d24, 0xff827717
    }, {    // Yellow
            0xfffffde7, 0xfffff9c4, 0xfffff59d, 0xfffff176,
            0xffffee58, 0xffffeb3b, 0xfffdd835, 0xfffbc02d,
            0xfff9a825, 0xfff57f17
    }, {    // Amber
            0xfffff8e1, 0xffffecb3, 0xffffe082, 0xffffd54f,
            0xffffca28, 0xffffc107, 0xffffb300, 0xffffa000,
            0xffff8f00, 0xffff6f00
    }, {    // Orange
            0xfffff3e0, 0xffffe0b2, 0xffffcc80, 0xffffb74d,
            0xffffa726, 0xffff9800, 0xfffb8c00, 0xfff57c00,
            0xffef6c00, 0xffe65100
    }, {    // Deep Orange
            0xfffbe9e7, 0xffffccbc, 0xffffab91, 0xffff8a65,
            0xffff7043, 0xffff5722, 0xfff4511e, 0xffe64a19,
            0xffd84315, 0xffbf360c
    }, {    // Brown
            0xffefebe9, 0xffd7ccc8, 0xffbcaaa4, 0xffa1887f,
            0xff8d6e63, 0xff795548, 0xff6d4c41, 0xff5d4037,
            0xff4e342e, 0xff3e2723
    }, {    // Grey
            0xfffafafa, 0xfff5f5f5, 0xffeeeeee, 0xffe0e0e0,
            0xffbdbdbd, 0xff9e9e9e, 0xff757575, 0xff616161,
            0xff424242, 0xff212121, 0xff000000, 0xffffffff
    }, {    // Blue Grey
            0xffeceff1, 0xffeceff1, 0xffb0bec5, 0xff90a4ae,
            0xff78909c, 0xff607d8b, 0xff546e7a, 0xff455a64,
            0xff37474f, 0xff263238
    }};

    /**
     * These are the colors that go in the initial palette.
     */
    public static final int[] PALETTE = {
            COLORS[0][5], // Red
            COLORS[1][5], // Pink
            COLORS[2][5], // Purple
            COLORS[3][5], // Deep purple
            COLORS[4][5], // Indigo
            COLORS[5][5], // Blue
            COLORS[6][5], // Light Blue
            COLORS[7][5], // Cyan
            COLORS[8][5], // Teal
            COLORS[9][5], // Green
            COLORS[10][5], // Light Green
            COLORS[11][5], // Lime
            COLORS[12][5], // Yellow
            COLORS[13][5], // Amber
            COLORS[14][5], // Orange
            COLORS[15][5], // Deep Orange
            COLORS[16][5], // Brown
            COLORS[17][5], // Grey
            COLORS[18][5] // Blue Grey
    };

    /**
     * This configures whether the text is black (0) or white (1) for each color above.
     */
    private static final int[][] TEXT_MODE = {{
            // Red
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Pink
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Purple
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Deep Purple
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Indigo
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Blue
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Light Blue
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Cyan
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Teal
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Green
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Light Green
            0, 0, 0, 0, 1, 1, 1, 1, 1, 1
    }, {    // Lime
            0, 0, 0, 0, 0, 0, 1, 1, 1, 1
    }, {    // Yellow
            0, 0, 0, 0, 0, 0, 0, 1, 1, 1
    }, {    // Amber
            0, 0, 0, 0, 0, 1, 1, 1, 1, 1
    }, {    // Orange
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }, {    // Deep Orange
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Brown
            0, 0, 1, 1, 1, 1, 1, 1, 1, 1
    }, {    // Grey
            0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 0
    }, {    // Blue Grey
            0, 0, 0, 1, 1, 1, 1, 1, 1, 1
    }};

    private static int mColor;
    private static int mActiveColor;
    private static int mBackgroundColor;
    private static Theme mTheme;

    private static int mTextOnColorPrimary;
    private static int mTextOnColorSecondary;
    private static int mTextOnBackgroundPrimary;
    private static int mtextOnBackgroundSecondary;
    private static int mSentBubbleRes;
    private static int mSentBubbleAltRes;
    private static boolean mSentBubbleColored;
    private static int mReceivedBubbleRes;
    private static int mReceivedBubbleAltRes;
    private static boolean mReceivedBubbleColored;
    private static Drawable mRippleBackground;

    private static Resources mResources;
    private static SharedPreferences mPrefs;

    private static Context mContext;

    public static void init(Context context) {
        mContext = context;

        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mResources = context.getResources();

        mColor = Integer.parseInt(mPrefs.getString(SettingsFragment.THEME, "" + ThemeManager.DEFAULT_COLOR));
        mActiveColor = mColor;

        initializeTheme(Theme.fromString(mPrefs.getString(SettingsFragment.BACKGROUND, "offwhite")));
    }

    public static void setTheme(Theme theme) {
        final int startColor = mBackgroundColor;
        initializeTheme(theme);
        final int endColor = mBackgroundColor;

        if (startColor != endColor) {
            ValueAnimator backgroundAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
            backgroundAnimation.setDuration(TRANSITION_LENGTH);
            backgroundAnimation.addUpdateListener(animation -> {
                mBackgroundColor = (Integer) animation.getAnimatedValue();
                LiveViewManager.refreshViews(QKPreference.BACKGROUND);
            });
            backgroundAnimation.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mBackgroundColor = endColor;
                    LiveViewManager.refreshViews(QKPreference.BACKGROUND);
                    WidgetProvider.notifyThemeChanged(mContext);
                }
            });
            backgroundAnimation.start();
        } else {
            LiveViewManager.refreshViews(QKPreference.BACKGROUND);
            WidgetProvider.notifyThemeChanged(mContext);
        }
    }

    public static void initializeTheme(Theme theme) {
        mTheme = theme;

        switch (theme) {
            case LIGHT:
                mBackgroundColor = mResources.getColor(R.color.grey_light_mega_ultra);
                mTextOnBackgroundPrimary = mResources.getColor(R.color.theme_light_text_primary);
                mtextOnBackgroundSecondary = mResources.getColor(R.color.theme_light_text_secondary);
                mRippleBackground = mResources.getDrawable(R.drawable.button_background_transparent);
                break;

            case DARK:
                mBackgroundColor = mResources.getColor(R.color.grey_material);
                mTextOnBackgroundPrimary = mResources.getColor(R.color.theme_dark_text_primary);
                mtextOnBackgroundSecondary = mResources.getColor(R.color.theme_dark_text_secondary);
                mRippleBackground = mResources.getDrawable(R.drawable.button_background_transparent_light);
                break;

            case BLACK:
                mBackgroundColor = mResources.getColor(R.color.black);
                mTextOnBackgroundPrimary = mResources.getColor(R.color.theme_dark_text_primary);
                mtextOnBackgroundSecondary = mResources.getColor(R.color.theme_dark_text_secondary);
                mRippleBackground = mResources.getDrawable(R.drawable.button_background_transparent_light);
                break;
        }

        mTextOnColorPrimary = mResources.getColor(isColorDarkEnough(mColor) ?
                R.color.theme_dark_text_primary : R.color.theme_light_text_primary);
        mTextOnColorSecondary = mResources.getColor(isColorDarkEnough(mColor) ?
                R.color.theme_dark_text_secondary : R.color.theme_light_text_secondary);

        setSentBubbleColored(mPrefs.getBoolean(SettingsFragment.COLOR_SENT, true));
        setReceivedBubbleColored(mPrefs.getBoolean(SettingsFragment.COLOR_RECEIVED, false));
        setBubbleStyleNew(mPrefs.getBoolean(SettingsFragment.BUBBLES_NEW, true));

        LiveViewManager.refreshViews(QKPreference.BACKGROUND);
    }

    public static void setIcon(final QKActivity context) {
        new QKDialog()
                .setContext(context)
                .setTitle(R.string.update_icon_title)
                .setMessage(R.string.update_icon_message)
                .setButtonBarOrientation(LinearLayout.VERTICAL)
                .setPositiveButton(R.string.okay, v -> {
                    PackageManager packageManager = context.getPackageManager();

                    String[] colors = {
                            "Red", "Pink", "Purple", "DeepPurple", "Indigo", "Blue",
                            "LightBlue", "Cyan", "Teal", "Green", "LightGreen", "Lime",
                            "Yellow", "Amber", "Orange", "DeepOrange", "Brown", "Grey",
                            "BlueGrey"
                    };

                    // Disable all of the color aliases, except for the alias with the current
                    // color.
                    String enabledComponent = null;
                    for (int i = 0; i < colors.length; i++) {
                        String componentClassName = String.format(
                                "com.moez.QKSMS.ui.MainActivity-%s", colors[i]
                        );

                        // Save the enabled component so we can kill the app with this one when
                        // it's all done.
                        if (getSwatchColor(mColor) == PALETTE[i]) {
                            enabledComponent = componentClassName;

                        } else {
                            packageManager.setComponentEnabledSetting(
                                    new ComponentName(context, componentClassName),
                                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                    // Don't kill the app while we're in the loop! This will
                                    // prevent the other component enabled settings from
                                    // changing, i.e. they will all be disabled and the app
                                    // won't show up to the user.
                                    PackageManager.DONT_KILL_APP
                            );
                        }
                    }

                    // Broadcast an intent to a receiver that will:
                    // 1) enable the last component; and
                    // 2) relaunch QKSMS with the new component name.
                    Intent intent = new Intent(IconColorReceiver.ACTION_ICON_COLOR_CHANGED);
                    intent.putExtra(IconColorReceiver.EXTRA_COMPONENT_NAME, enabledComponent);
                    context.sendBroadcast(intent);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public static int getBackgroundColor() {
        return mBackgroundColor;
    }

    public static int getTextOnColorPrimary() {
        return mTextOnColorPrimary;
    }

    public static int getTextOnColorSecondary() {
        return mTextOnColorSecondary;
    }

    public static int getTextOnBackgroundPrimary() {
        return mTextOnBackgroundPrimary;
    }

    public static int getTextOnBackgroundSecondary() {
        return mtextOnBackgroundSecondary;
    }

    public static int getSentBubbleRes() {
        return mSentBubbleRes;
    }

    public static int getSentBubbleAltRes() {
        return mSentBubbleAltRes;
    }

    public static int getSentBubbleColor() {
        return mSentBubbleColored ? mActiveColor : getNeutralBubbleColor();
    }

    public static int getReceivedBubbleRes() {
        return mReceivedBubbleRes;
    }

    public static int getReceivedBubbleAltRes() {
        return mReceivedBubbleAltRes;
    }

    public static int getReceivedBubbleColor() {
        return mReceivedBubbleColored ? mActiveColor : getNeutralBubbleColor();
    }

    public static void setBubbleStyleNew(boolean styleNew) {
        mSentBubbleRes = styleNew ? R.drawable.message_sent_2 : R.drawable.message_sent;
        mSentBubbleAltRes = styleNew ? R.drawable.message_sent_alt_2 : R.drawable.message_sent_alt;
        mReceivedBubbleRes = styleNew ? R.drawable.message_received_2 : R.drawable.message_received;
        mReceivedBubbleAltRes = styleNew ? R.drawable.message_received_alt_2 : R.drawable.message_received_alt;
    }

    public static void setSentBubbleColored(boolean colored) {
        mSentBubbleColored = colored;
    }

    public static void setReceivedBubbleColored(boolean colored) {
        mReceivedBubbleColored = colored;
    }

    public static int getNeutralBubbleColor() {
        if (mTheme == null) {
            return 0xeeeeee;
        }

        switch (mTheme) {
            case DARK:
                return mResources.getColor(R.color.grey_dark);

            case BLACK:
                return mResources.getColor(R.color.grey_material);

            default:
                return mResources.getColor(R.color.white_pure);
        }
    }

    public static Drawable getRippleBackground() {
        return mRippleBackground;
    }

    public static int getColor() {
        return mActiveColor;
    }

    public static int getThemeColor() {
        return mColor;
    }

    public static Theme getTheme() {
        return mTheme;
    }

    public static boolean isNightMode() {
        return mTheme == Theme.DARK || mTheme == Theme.BLACK;
    }

    public static void showColorPickerDialog(final QKActivity context) {
        final QKDialog dialog = new QKDialog();

        ColorPickerPalette palette = new ColorPickerPalette(context);
        palette.setGravity(Gravity.CENTER);
        palette.init(19, 4, color -> {
            palette.init(getSwatch(color).length, 4, color2 -> {
                setColor(context, color2);
                dialog.dismiss();
            });

            palette.drawPalette(getSwatch(color), mColor);
        });

        palette.drawPalette(PALETTE, getSwatchColor(mColor));

        dialog.setContext(context)
                .setTitle(R.string.pref_theme)
                .setCustomView(palette)
                .setNegativeButton(R.string.cancel, null);

        dialog.show();
    }

    public static void showColorPickerDialogForConversation(final QKActivity context, ConversationPrefsHelper prefs) {
        final QKDialog dialog = new QKDialog();

        ColorPickerPalette palette = new ColorPickerPalette(context);
        palette.setGravity(Gravity.CENTER);
        palette.init(19, 4, color -> {
            palette.init(getSwatch(color).length, 4, color2 -> {
                prefs.putString(QKPreference.THEME.getKey(), "" + color2);
                setActiveColor(color2);
                LiveViewManager.refreshViews(QKPreference.CONVERSATION_THEME);
                dialog.dismiss();
            });

            palette.drawPalette(getSwatch(color), prefs.getColor());
        });

        palette.drawPalette(PALETTE, getSwatchColor(prefs.getColor()));

        dialog.setContext(context)
                .setTitle(R.string.pref_theme)
                .setCustomView(palette)
                .setNegativeButton(R.string.cancel, null);

        dialog.show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarTintEnabled(QKActivity activity, boolean enabled) {
        int colorFrom = enabled ? mResources.getColor(R.color.black) : mColor;
        int colorTo = enabled ? mColor : mResources.getColor(R.color.black);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(TRANSITION_LENGTH);
        colorAnimation.addUpdateListener(animation -> {
            activity.getWindow().setStatusBarColor(ColorUtils.darken((Integer) animation.getAnimatedValue()));
        });
        colorAnimation.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setNavigationBarTintEnabled(QKActivity activity, boolean enabled) {
        int colorFrom = enabled ? mResources.getColor(R.color.black) : mColor;
        int colorTo = enabled ? mColor : mResources.getColor(R.color.black);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(TRANSITION_LENGTH);
        colorAnimation.addUpdateListener(animation -> {
            activity.getWindow().setNavigationBarColor(ColorUtils.darken((Integer) animation.getAnimatedValue()));
        });
        colorAnimation.start();
    }

    public static String getColorString(int color) {
        return String.format("#%08x", color).toUpperCase();
    }

    public static void setColor(QKActivity activity, int color) {

        AnalyticsManager.getInstance().sendEvent(
                AnalyticsManager.CATEGORY_PREFERENCE_CHANGE,
                SettingsFragment.CATEGORY_THEME,
                getColorString(color)
        );

        int colorFrom = mColor;
        mColor = color;
        mActiveColor = color;

        mPrefs.edit().putString(SettingsFragment.THEME, "" + color).apply();

        setSentBubbleColored(mPrefs.getBoolean(SettingsFragment.COLOR_SENT, true));
        setReceivedBubbleColored(mPrefs.getBoolean(SettingsFragment.COLOR_RECEIVED, false));
        mTextOnColorPrimary = mResources.getColor(isColorDarkEnough(mColor) ?
                R.color.theme_dark_text_primary : R.color.theme_light_text_primary);
        mTextOnColorSecondary = mResources.getColor(isColorDarkEnough(mColor) ?
                R.color.theme_dark_text_secondary : R.color.theme_light_text_secondary);

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new CIELChEvaluator(colorFrom, color), 0);
        colorAnimation.setDuration(TRANSITION_LENGTH);
        colorAnimation.setInterpolator(new DecelerateInterpolator());
        colorAnimation.addUpdateListener(animation -> {
            setActiveColor((Integer) animation.getAnimatedValue());
        });
        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                WidgetProvider.notifyThemeChanged(mContext);
            }
        });
        colorAnimation.start();


        if (activity.findViewById(R.id.toolbar_title) != null) {
            //final Toolbar toolbar = (Toolbar) mActivity.findViewById(R.id.title);
            final QKTextView title = (QKTextView) activity.findViewById(R.id.toolbar_title);

            if (title.getCurrentTextColor() != mTextOnColorPrimary) {
                ValueAnimator titleColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), title.getCurrentTextColor(), mTextOnColorPrimary);
                titleColorAnimation.setDuration(TRANSITION_LENGTH);
                titleColorAnimation.setInterpolator(new DecelerateInterpolator());
                titleColorAnimation.addUpdateListener(animation -> {
                    int color1 = (Integer) animation.getAnimatedValue();
                    title.setTextColor(color1);
                    activity.colorMenuIcons(activity.getMenu(), color1);
                });
                titleColorAnimation.start();
            }
        }
    }

    public static void setActiveColor(int color) {
        mActiveColor = color;
        LiveViewManager.refreshViews(QKPreference.THEME);
    }

    private static boolean isColorDarkEnough(int color) {
        for (int i = 0; i < COLORS.length; i++) {
            for (int j = 0; j < COLORS[i].length; j++) {
                if (color == COLORS[i][j]) {
                    return TEXT_MODE[i][j] == 1;
                }
            }
        }

        return true;
    }

    public static int getSwatchColor(int color) {
        for (int i = 0; i < COLORS.length; i++) {
            for (int j = 0; j < COLORS[i].length; j++) {
                if (color == COLORS[i][j]) {
                    return PALETTE[i];
                }
            }
        }

        return color;
    }

    private static int[] getSwatch(int color) {
        for (int[] swatch : COLORS) {
            for (int swatchColor : swatch) {
                if (color == swatchColor) {
                    return swatch;
                }
            }
        }

        return PALETTE;
    }
}
