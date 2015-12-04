package com.moez.QKSMS.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.preference.PreferenceManager;
import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.settings.SettingsFragment;

public class FontManager {

    public static final int TEXT_SIZE_SMALL = 0;
    public static final int TEXT_SIZE_NORMAL = 1;
    public static final int TEXT_SIZE_LARGE = 2;
    public static final int TEXT_SIZE_LARGEST = 3;

    // Attribute codes
    public static final int TEXT_TYPE_PRIMARY = 0x1;
    public static final int TEXT_TYPE_SECONDARY = 0x2;
    public static final int TEXT_TYPE_TERTIARY = 0x3;
    public static final int TEXT_TYPE_CATEGORY = 0x4;
    public static final int TEXT_TYPE_DIALOG_TITLE = 0x5;
    public static final int TEXT_TYPE_DIALOG_MESSAGE = 0x6;
    public static final int TEXT_TYPE_DIALOG_BUTTON = 0x7;
    public static final int TEXT_TYPE_TOOLBAR = 0x8;

    public static int getFontFamily(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(prefs.getString(SettingsFragment.FONT_FAMILY, "0"));
    }

    public static int getTextSize(Context context, int type) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int fontSize = Integer.parseInt(prefs.getString(SettingsFragment.FONT_SIZE, "1"));

        switch (type) {
            case TEXT_TYPE_TERTIARY:
                if (fontSize == TEXT_SIZE_SMALL) return 10;
                if (fontSize == TEXT_SIZE_NORMAL) return 12;
                if (fontSize == TEXT_SIZE_LARGE) return 14;
                if (fontSize == TEXT_SIZE_LARGEST) return 16;
                break; // All further cases do the same checks, so we might as will just break and return the default value
            case TEXT_TYPE_SECONDARY:
            case TEXT_TYPE_DIALOG_BUTTON:
            case TEXT_TYPE_CATEGORY:
                if (fontSize == TEXT_SIZE_SMALL) return 12;
                if (fontSize == TEXT_SIZE_NORMAL) return 14;
                if (fontSize == TEXT_SIZE_LARGE) return 16;
                if (fontSize == TEXT_SIZE_LARGEST) return 18;
                break;
            case TEXT_TYPE_PRIMARY:
            case TEXT_TYPE_DIALOG_MESSAGE:
                if (fontSize == TEXT_SIZE_SMALL) return 14;
                if (fontSize == TEXT_SIZE_NORMAL) return 16;
                if (fontSize == TEXT_SIZE_LARGE) return 18;
                if (fontSize == TEXT_SIZE_LARGEST) return 22;
                break;
            case TEXT_TYPE_DIALOG_TITLE:
            case TEXT_TYPE_TOOLBAR:
                if (fontSize == TEXT_SIZE_SMALL) return 18;
                if (fontSize == TEXT_SIZE_NORMAL) return 20;
                if (fontSize == TEXT_SIZE_LARGE) return 22;
                if (fontSize == TEXT_SIZE_LARGEST) return 26;
                break;
        }

        return 14;
    }

    public static boolean getIsFontHeavy(int type) {
        switch (type) {
            case FontManager.TEXT_TYPE_PRIMARY:
            case FontManager.TEXT_TYPE_SECONDARY:
            case FontManager.TEXT_TYPE_TERTIARY:
            case FontManager.TEXT_TYPE_DIALOG_MESSAGE:
            case FontManager.TEXT_TYPE_TOOLBAR:
                return false;

            case FontManager.TEXT_TYPE_CATEGORY:
            case FontManager.TEXT_TYPE_DIALOG_TITLE:
            case FontManager.TEXT_TYPE_DIALOG_BUTTON:
                return true;
        }
        return false;
    }

    public static ColorStateList getTextColor(Context context, int type) {
        // Colors and font weight
        switch (type) {
            case FontManager.TEXT_TYPE_PRIMARY:
                boolean isNight = ThemeManager.getTheme() == ThemeManager.Theme.GREY ||
                                  ThemeManager.getTheme() == ThemeManager.Theme.BLACK;
                int id = isNight ? R.color.text_primary_dark : R.color.text_primary_light;
                return context.getResources().getColorStateList(id);
            case FontManager.TEXT_TYPE_SECONDARY:
                return ColorStateList.valueOf(ThemeManager.getTextOnBackgroundSecondary());
            case FontManager.TEXT_TYPE_TERTIARY:
                return ColorStateList.valueOf(ThemeManager.getTextOnBackgroundSecondary());
            case FontManager.TEXT_TYPE_CATEGORY:
                return ColorStateList.valueOf(ThemeManager.getColor());
            case FontManager.TEXT_TYPE_DIALOG_TITLE:
                return ColorStateList.valueOf(ThemeManager.getTextOnBackgroundPrimary());
            case FontManager.TEXT_TYPE_DIALOG_MESSAGE:
                return ColorStateList.valueOf(ThemeManager.getTextOnBackgroundSecondary());
            case FontManager.TEXT_TYPE_DIALOG_BUTTON:
                return ColorStateList.valueOf(ThemeManager.getTextOnBackgroundPrimary());
            case FontManager.TEXT_TYPE_TOOLBAR:
                return ColorStateList.valueOf(ThemeManager.getTextOnColorPrimary());
        }
        return ColorStateList.valueOf(ThemeManager.getTextOnBackgroundPrimary());
    }

    public static int getFontWeight(Context context, boolean heavy) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int fontWeight = Integer.parseInt(prefs.getString(SettingsFragment.FONT_WEIGHT, "0"));
        int fontFamily = getFontFamily(context);

        if (!heavy) {
            return fontWeight;
        }

        // Otherwise, getConversation the heavy font weight.
        if (fontWeight == TypefaceManager.TextWeight.LIGHT) {
            return TypefaceManager.TextWeight.NORMAL;
        } else if (fontFamily == TypefaceManager.FontFamily.ROBOTO) {
            return TypefaceManager.TextWeight.MEDIUM;
        } else {
            return TypefaceManager.TextWeight.BOLD;
        }
    }
}
