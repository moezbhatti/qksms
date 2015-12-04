package com.moez.QKSMS.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.AnalyticsManager;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.interfaces.LiveView;
import com.moez.QKSMS.receiver.IconColorReceiver;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.dialog.QKDialog;
import com.moez.QKSMS.ui.settings.SettingsFragment;
import com.moez.QKSMS.ui.view.QKTextView;
import com.moez.QKSMS.ui.view.colorpicker.ColorPickerPalette;
import com.moez.QKSMS.ui.view.colorpicker.ColorPickerSwatch;
import com.moez.QKSMS.ui.widget.WidgetProvider;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.Set;

public class ThemeManager {
    private final static String TAG = "ThemeManager";

    public static final int DEFAULT_COLOR = 0xff009688;
    public static final int TRANSITION_LENGTH = 500;

    public enum Theme {
        WHITE,
        OFFWHITE,
        GREY,
        BLACK;

        public static final String PREF_WHITE = "white";
        public static final String PREF_OFFWHITE = "offwhite";
        public static final String PREF_GREY = "grey";
        public static final String PREF_BLACK = "black";

        public static Theme fromString(String color) {
            switch (color) {
                case PREF_WHITE:
                    return WHITE;
                case PREF_OFFWHITE:
                    return OFFWHITE;
                case PREF_GREY:
                    return GREY;
                case PREF_BLACK:
                    return BLACK;
                default:
                    Log.w(TAG, "Tried to set theme with invalid string: " + color);
                    return OFFWHITE;
            }
        }
    }

    private static int sColor;
    private static int sBackgroundColor;
    private static Theme sTheme;

    private static int sTextOnColorPrimary;
    private static int sTextOnColorSecondary;
    private static int sTextOnBackgroundPrimary;
    private static int sTextOnBackgroundSecondary;
    private static int sSentBubbleRes;
    private static int sSentBubbleAltRes;
    private static int sSentBubbleColor;
    private static int sReceivedBubbleRes;
    private static int sReceivedBubbleAltRes;
    private static int sReceivedBubbleColor;
    private static Drawable sRippleBackground;

    private static SystemBarTintManager sTintManager;
    private static Drawable sStatusBarTintDrawable;
    private static Resources sResources;
    private static SharedPreferences sPrefs;

    private static boolean status_tint = false;
    private static boolean status_compat = true;
    private static boolean system_flat = false;

    private static QKActivity mActivity;
    private static Context mContext;

    // Colours copied from http://www.google.com/design/spec/style/color.html#color-ui-color-palette
    private static final int[][] COLOURS = {{
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
            COLOURS[0][5], // Red
            COLOURS[1][5], // Pink
            COLOURS[2][5], // Purple
            COLOURS[3][5], // Deep purple
            COLOURS[4][5], // Indigo
            COLOURS[5][5], // Blue
            COLOURS[6][5], // Light Blue
            COLOURS[7][5], // Cyan
            COLOURS[8][5], // Teal
            COLOURS[9][5], // Green
            COLOURS[10][5], // Light Green
            COLOURS[11][5], // Lime
            COLOURS[12][5], // Yellow
            COLOURS[13][5], // Amber
            COLOURS[14][5], // Orange
            COLOURS[15][5], // Deep Orange
            COLOURS[16][5], // Brown
            COLOURS[17][5], // Grey
            COLOURS[18][5] // Blue Grey
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

    /**
     * Loads all theme properties. Should be called during onCreate
     * of each activity that contains fragments that use ThemeManager
     */
    public static void loadThemeProperties(Context context) {
        sPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sResources = context.getResources();

        mContext = context;

        sColor = Integer.parseInt(sPrefs.getString(SettingsFragment.THEME, "" + ThemeManager.DEFAULT_COLOR));

        if (context instanceof QKActivity) {
            mActivity = (QKActivity) context;
            mActivity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(sColor));
        }

        initializeTheme(Theme.fromString(sPrefs.getString(SettingsFragment.BACKGROUND, "offwhite")));

        status_tint = sPrefs.getBoolean(SettingsFragment.STATUS_TINT, Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        status_compat = sPrefs.getBoolean(SettingsFragment.STATUS_COMPAT, false);
        system_flat = sPrefs.getBoolean(SettingsFragment.SYSTEM_BAR_FLAT, false);

        if (mActivity != null) {
            sTintManager = new SystemBarTintManager(mActivity);
            sTintManager.setStatusBarTintEnabled(!status_compat);
            sTintManager.setNavigationBarTintEnabled(false);
            sTintManager.setStatusBarTintColor(status_tint ? sColor : sResources.getColor(R.color.black));
            sTintManager.setNavigationBarTintColor(sResources.getColor(R.color.black));
        }

        sStatusBarTintDrawable = ContextCompat.getDrawable(context, R.drawable.status_bar_background);
        if (sStatusBarTintDrawable != null) {
            sStatusBarTintDrawable.setColorFilter(status_tint ? sColor : sResources.getColor(R.color.black), PorterDuff.Mode.MULTIPLY);
        }

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && system_flat) {
            sTintManager.setStatusBarTintDrawable(sStatusBarTintDrawable);
        }
    }

    public static void setTheme(Theme theme) {
        int startColor = sBackgroundColor;
        initializeTheme(theme);
        int endColor = sBackgroundColor;

        if (mActivity instanceof MainActivity) {
            final View background = mActivity.findViewById(R.id.menu_frame).getRootView();
            final View menu = mActivity.findViewById(R.id.menu_frame);
            final View content = mActivity.findViewById(R.id.content_frame);
            final View fragment = ((MainActivity) mActivity).getContent().getView();

            if (startColor != endColor) {
                ValueAnimator backgroundAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), startColor, endColor);
                backgroundAnimation.setDuration(TRANSITION_LENGTH);
                backgroundAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int color = (Integer) animation.getAnimatedValue();
                        if (fragment != null) {
                            fragment.setBackgroundColor(color);
                        }
                        background.setBackgroundColor(color);
                        menu.setBackgroundColor(color);
                        content.setBackgroundColor(color);
                    }
                });
                backgroundAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // This updates the colors and fonts of all the views.
                        LiveViewManager.refreshViews(SettingsFragment.BACKGROUND);
                        WidgetProvider.notifyThemeChanged(mContext);
                    }
                });
                backgroundAnimation.start();
            } else {
                // This updates the colors and fonts of all the views.
                LiveViewManager.refreshViews(SettingsFragment.BACKGROUND);
                background.setBackgroundColor(endColor);
                menu.setBackgroundColor(endColor);
                content.setBackgroundColor(endColor);
                WidgetProvider.notifyThemeChanged(mContext);
            }
        } else {
            // This updates the colors and fonts of all the views.
            LiveViewManager.refreshViews(SettingsFragment.BACKGROUND);
            WidgetProvider.notifyThemeChanged(mContext);
        }
    }

    public static void initializeTheme(Theme theme) {
        sTheme = theme;

        switch (theme) {
            case WHITE:
                sBackgroundColor = sResources.getColor(R.color.white_pure);
                if (mContext != null) mContext.setTheme(mContext instanceof MainActivity ?
                        R.style.AppThemeWhite : R.style.AppThemeWhiteDialog);
                break;
            case OFFWHITE:
                sBackgroundColor = sResources.getColor(R.color.grey_light_mega_ultra);
                if (mContext != null) mContext.setTheme(mContext instanceof MainActivity ?
                        R.style.AppThemeLight : R.style.AppThemeLightDialog);
                break;
            case GREY:
                sBackgroundColor = sResources.getColor(R.color.grey_material);
                if (mContext != null) mContext.setTheme(mContext instanceof MainActivity ?
                        R.style.AppThemeDark : R.style.AppThemeDarkDialog);
                break;
            case BLACK:
                sBackgroundColor = sResources.getColor(R.color.black);
                if (mContext != null) mContext.setTheme(mContext instanceof MainActivity ?
                        R.style.AppThemeDarkAmoled : R.style.AppThemeDarkAmoledDialog);
                break;
        }

        switch (sTheme) {
            case WHITE:
            case OFFWHITE:
                sTextOnBackgroundPrimary = sResources.getColor(R.color.theme_light_text_primary);
                sTextOnBackgroundSecondary = sResources.getColor(R.color.theme_light_text_secondary);
                sRippleBackground = sResources.getDrawable(R.drawable.button_background_transparent);
                break;
            case GREY:
            case BLACK:
                sTextOnBackgroundPrimary = sResources.getColor(R.color.theme_dark_text_primary);
                sTextOnBackgroundSecondary = sResources.getColor(R.color.theme_dark_text_secondary);
                sRippleBackground = sResources.getDrawable(R.drawable.button_background_transparent_light);
                break;
        }

        sTextOnColorPrimary = sResources.getColor(isColorDarkEnough(sColor) ?
                R.color.theme_dark_text_primary : R.color.theme_light_text_primary);
        sTextOnColorSecondary = sResources.getColor(isColorDarkEnough(sColor) ?
                R.color.theme_dark_text_secondary : R.color.theme_light_text_secondary);

        setSentBubbleColored(sPrefs.getBoolean(SettingsFragment.COLOUR_SENT, true));
        setReceivedBubbleColored(sPrefs.getBoolean(SettingsFragment.COLOUR_RECEIVED, false));
        setBubbleStyleNew(sPrefs.getBoolean(SettingsFragment.BUBBLES_NEW, true));

        if (mActivity != null) {
            // We need to set this here because the title bar is initialized before the ThemeManager,
            // so it's not using the correct color yet
            ((QKTextView) mActivity.findViewById(R.id.toolbar_title)).setTextColor(sTextOnColorPrimary);
        }
    }

    public static void setIcon(final QKActivity context) {
        new QKDialog()
                .setContext(context)
                .setTitle(R.string.update_icon_title)
                .setMessage(R.string.update_icon_message)
                .setButtonBarOrientation(LinearLayout.VERTICAL)
                .setPositiveButton(R.string.okay, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                            if (getSwatchColour(sColor) == PALETTE[i]) {
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
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show(((MainActivity) context).getFragmentManager(), "icon");
    }

    public static int getBackgroundColor() {
        return sBackgroundColor;
    }

    public static int getTextOnColorPrimary() {
        return sTextOnColorPrimary;
    }

    public static int getTextOnColorSecondary() {
        return sTextOnColorSecondary;
    }

    public static int getTextOnBackgroundPrimary() {
        return sTextOnBackgroundPrimary;
    }

    public static int getTextOnBackgroundSecondary() {
        return sTextOnBackgroundSecondary;
    }

    public static int getSentBubbleRes() {
        return sSentBubbleRes;
    }

    public static int getSentBubbleAltRes() {
        return sSentBubbleAltRes;
    }

    public static int getSentBubbleColor() {
        return sSentBubbleColor;
    }

    public static int getReceivedBubbleRes() {
        return sReceivedBubbleRes;
    }

    public static int getReceivedBubbleAltRes() {
        return sReceivedBubbleAltRes;
    }

    public static int getReceivedBubbleColor() {
        return sReceivedBubbleColor;
    }

    public static void setBubbleStyleNew(boolean styleNew) {
        sSentBubbleRes = styleNew ? R.drawable.message_sent_2 : R.drawable.message_sent;
        sSentBubbleAltRes = styleNew ? R.drawable.message_sent_alt_2 : R.drawable.message_sent_alt;
        sReceivedBubbleRes = styleNew ? R.drawable.message_received_2 : R.drawable.message_received;
        sReceivedBubbleAltRes = styleNew ? R.drawable.message_received_alt_2 : R.drawable.message_received_alt;
    }

    public static void setSentBubbleColored(boolean colored) {
        sSentBubbleColor = colored ? sColor : getNeutralBubbleColor();
    }

    public static void setReceivedBubbleColored(boolean colored) {
        sReceivedBubbleColor = colored ? sColor : getNeutralBubbleColor();
    }

    public static int getNeutralBubbleColor() {
        if (sTheme == null) {
            return 0xeeeeee;
        }

        switch (sTheme) {
            case WHITE:
                return sResources.getColor(R.color.grey_light_mega_ultra);
            case OFFWHITE:
                return sResources.getColor(R.color.white_pure);
            default:
                return sResources.getColor(R.color.grey_dark);
        }
    }

    public static Drawable getRippleBackground() {
        return sRippleBackground;
    }

    public static int getColor() {
        return sColor;
    }

    public static Theme getTheme() {
        return sTheme;
    }

    public static void showColourSwatchesDialog(final QKActivity context) {
        final QKDialog dialog = new QKDialog();

        ColorPickerPalette palette = new ColorPickerPalette(context);
        palette.setGravity(Gravity.CENTER);
        palette.init(19, 4, new ColorPickerSwatch.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                showColourPickerDialog(context, color);
                dialog.dismiss();
            }
        });

        palette.drawPalette(PALETTE, getSwatchColour(sColor));

        dialog.setContext(context)
                .setTitle(R.string.pref_theme)
                .setCustomView(palette)
                .setNegativeButton(R.string.cancel, null);

        dialog.show(context.getFragmentManager(), "colorpicker");
    }

    private static void showColourPickerDialog(final QKActivity context, int swatchColour) {
        final QKDialog dialog = new QKDialog();

        ColorPickerPalette palette = new ColorPickerPalette(context);
        palette.setGravity(Gravity.CENTER);
        palette.init(getSwatch(swatchColour).length, 4, new ColorPickerSwatch.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                setColour(color);
                dialog.dismiss();
            }
        });

        palette.drawPalette(getSwatch(swatchColour), sColor);

        dialog.setContext(context)
                .setTitle(R.string.pref_theme)
                .setCustomView(palette)
                .setNegativeButton(R.string.cancel, null)
                .show(context.getFragmentManager(), "colorpicker");
    }

    public static void setStatusBarTintEnabled(boolean enabled) {
        if (status_tint != enabled) {
            status_tint = enabled;
            int colorFrom = enabled ? sResources.getColor(R.color.black) : sColor;
            int colorTo = enabled ? sColor : sResources.getColor(R.color.black);

            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.setDuration(TRANSITION_LENGTH);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    sTintManager.setStatusBarTintColor((Integer) animation.getAnimatedValue());

                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && system_flat) {
                        sStatusBarTintDrawable.setColorFilter((Integer) animation.getAnimatedValue(), PorterDuff.Mode.MULTIPLY);
                        sTintManager.setStatusBarTintDrawable(sStatusBarTintDrawable);
                    }
                }
            });
            colorAnimation.start();
        }
    }

    public static void setSystemBarFlatEnabled(boolean enabled) {
        system_flat = enabled;

        if (status_tint) {
            status_tint = false;
            setStatusBarTintEnabled(true);
        }
    }

    public static void setStatusBarTintCompat(boolean enabled) {
        sTintManager.setStatusBarTintEnabled(!enabled);
    }

    public static String getColorString(int color) {
        return String.format("#%08x", color).toUpperCase();
    }

    public static void setColour(int color) {

        AnalyticsManager.getInstance().sendEvent(
                AnalyticsManager.CATEGORY_PREFERENCE_CHANGE,
                SettingsFragment.CATEGORY_THEME,
                getColorString(color)
        );

        int colourFrom = sColor;
        sColor = color;

        sPrefs.edit().putString(SettingsFragment.THEME, "" + color).apply();

        setSentBubbleColored(sPrefs.getBoolean(SettingsFragment.COLOUR_SENT, true));
        setReceivedBubbleColored(sPrefs.getBoolean(SettingsFragment.COLOUR_RECEIVED, false));
        sTextOnColorPrimary = sResources.getColor(isColorDarkEnough(sColor) ?
                R.color.theme_dark_text_primary : R.color.theme_light_text_primary);
        sTextOnColorSecondary = sResources.getColor(isColorDarkEnough(sColor) ?
                R.color.theme_dark_text_secondary : R.color.theme_light_text_secondary);

        // Some views are updated every frame of the animation; getConversation these views here. We
        // build this list once beforehand since it's a slightly expensive operation.
        final Set<LiveView> views = LiveViewManager.getViews(SettingsFragment.THEME);

        // Refresh all the views with the new color.
        for (LiveView view : views) {
            view.refresh();
        }

        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colourFrom, color);
        colorAnimation.setDuration(TRANSITION_LENGTH);
        colorAnimation.setInterpolator(new DecelerateInterpolator());
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int color = (Integer) animation.getAnimatedValue();

                if (mActivity != null) {
                    if (mActivity.getSupportActionBar() != null) {
                        mActivity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
                    }
                }

                if (status_tint) {
                    sTintManager.setStatusBarTintColor(color);

                    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && system_flat) {
                        sStatusBarTintDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                        sTintManager.setStatusBarTintDrawable(sStatusBarTintDrawable);
                    }
                }
            }
        });
        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                WidgetProvider.notifyThemeChanged(mContext);
            }
        });
        colorAnimation.start();


        if (mActivity != null && mActivity.findViewById(R.id.toolbar_title) != null) {
            //final Toolbar toolbar = (Toolbar) mActivity.findViewById(R.id.title);
            final QKTextView title = (QKTextView) mActivity.findViewById(R.id.toolbar_title);

            if (title.getCurrentTextColor() != ThemeManager.sTextOnColorPrimary) {
                ValueAnimator titleColorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), title.getCurrentTextColor(), ThemeManager.sTextOnColorPrimary);
                titleColorAnimation.setDuration(TRANSITION_LENGTH);
                titleColorAnimation.setInterpolator(new DecelerateInterpolator());
                titleColorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int color = (Integer) animation.getAnimatedValue();
                        title.setTextColor(color);
                        mActivity.colorMenuIcons(mActivity.getMenu(), color);
                    }
                });
                titleColorAnimation.start();
            }
        }
    }

    private static boolean isColorDarkEnough(int color) {
        for (int i = 0; i < COLOURS.length; i++) {
            for (int j = 0; j < COLOURS[i].length; j++) {
                if (color == COLOURS[i][j]) {
                    return TEXT_MODE[i][j] == 1;
                }
            }
        }

        return true;
    }

    public static int getSwatchColour(int colour) {
        for (int i = 0; i < COLOURS.length; i++) {
            for (int j = 0; j < COLOURS[i].length; j++) {
                if (colour == COLOURS[i][j]) {
                    return PALETTE[i];
                }
            }
        }

        return colour;
    }

    private static int[] getSwatch(int colour) {
        for (int[] swatch : COLOURS) {
            for (int swatchColor : swatch) {
                if (colour == swatchColor) {
                    return swatch;
                }
            }
        }

        return PALETTE;
    }
}
