package com.moez.QKSMS.common;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseArray;

/**
 * Stripped down version of https://github.com/johnkil/Android-RobotoTextView/blob/master/robototextview/src/main/java/com/devspark/robototextview/util/RobotoTypefaceManager.java
 */
public class TypefaceManager {

    private final static SparseArray<android.graphics.Typeface> mTypefaces = new SparseArray<>();

    public static Typeface obtainTypeface(Context context, int fontFamily, int textWeight) throws IllegalArgumentException {
        int typefaceValue = Typefaces.ROBOTO_REGULAR;
        switch (fontFamily) {
            case FontFamily.ROBOTO:
                switch (textWeight) {
                    case TextWeight.THIN:
                        typefaceValue = Typefaces.ROBOTO_THIN;
                        break;
                    case TextWeight.LIGHT:
                        typefaceValue = Typefaces.ROBOTO_LIGHT;
                        break;
                    case TextWeight.NORMAL:
                        typefaceValue = Typefaces.ROBOTO_REGULAR;
                        break;
                    case TextWeight.MEDIUM:
                    case TextWeight.BOLD:
                        typefaceValue = Typefaces.ROBOTO_MEDIUM;
                        break;
                }
                break;

            case FontFamily.ROBOTO_CONDENSED:
                switch (textWeight) {
                    case TextWeight.THIN:
                    case TextWeight.LIGHT:
                        typefaceValue = Typefaces.ROBOTO_CONDENSED_LIGHT;
                        break;
                    case TextWeight.NORMAL:
                        typefaceValue = Typefaces.ROBOTO_CONDENSED_REGULAR;
                        break;
                    case TextWeight.MEDIUM:
                    case TextWeight.BOLD:
                        typefaceValue = Typefaces.ROBOTO_CONDENSED_BOLD;
                        break;
                }
                break;

            case FontFamily.SYSTEM_FONT:
                switch (textWeight) {
                    case TextWeight.THIN:
                    case TextWeight.LIGHT:
                    case TextWeight.NORMAL:
                        typefaceValue = Typefaces.DEFAULT_REGULAR;
                        break;
                    case TextWeight.MEDIUM:
                    case TextWeight.BOLD:
                        typefaceValue = Typefaces.DEFAULT_BOLD;
                        break;
                }
                break;
        }

        return obtainTypeface(context, typefaceValue);
    }

    public static Typeface obtainTypeface(Context context, int typefaceValue) throws IllegalArgumentException {
        android.graphics.Typeface typeface = mTypefaces.get(typefaceValue);
        if (typeface == null) {
            typeface = createTypeface(context, typefaceValue);
            mTypefaces.put(typefaceValue, typeface);
        }
        return typeface;
    }

    private static Typeface createTypeface(Context context, int typefaceValue) throws IllegalArgumentException {
        switch (typefaceValue) {
            case Typefaces.ROBOTO_THIN:
                return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Thin.ttf");

            case Typefaces.ROBOTO_LIGHT:
                return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");

            case Typefaces.ROBOTO_REGULAR:
                return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf");

            case Typefaces.ROBOTO_MEDIUM:
                return Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Medium.ttf");

            case Typefaces.ROBOTO_CONDENSED_LIGHT:
                return Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Light.ttf");

            case Typefaces.ROBOTO_CONDENSED_REGULAR:
                return Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Regular.ttf");

            case Typefaces.ROBOTO_CONDENSED_BOLD:
                return Typeface.createFromAsset(context.getAssets(), "fonts/RobotoCondensed-Bold.ttf");

            case Typefaces.DEFAULT_REGULAR:
                return Typeface.DEFAULT;

            case Typefaces.DEFAULT_BOLD:
                return Typeface.DEFAULT_BOLD;

            default:
                throw new IllegalArgumentException("Unknown `typeface` attribute value " + typefaceValue);
        }
    }

    public class Typefaces {
        public final static int ROBOTO_THIN = 0;
        public final static int ROBOTO_LIGHT = 1;
        public final static int ROBOTO_REGULAR = 2;
        public final static int ROBOTO_MEDIUM = 3;

        public final static int ROBOTO_CONDENSED_REGULAR = 4;
        public final static int ROBOTO_CONDENSED_BOLD = 5;
        public final static int ROBOTO_CONDENSED_LIGHT = 6;

        public final static int DEFAULT_REGULAR = 7;
        public final static int DEFAULT_BOLD = 8;
    }

    public class FontFamily {
        public static final int ROBOTO = 0;
        public static final int ROBOTO_CONDENSED = 1;
        public static final int SYSTEM_FONT = 2;
    }

    public class TextWeight {
        public static final int NORMAL = 0;
        public static final int THIN = 1;
        public static final int LIGHT = 2;
        public static final int MEDIUM = 3;
        public static final int BOLD = 4;
    }

}
