package com.moez.QKSMS.common;

import android.content.Context;
import android.util.SparseArray;

/**
 * Stripped down version of https://github.com/johnkil/Android-RobotoTextView/blob/master/robototextview/src/main/java/com/devspark/robototextview/util/RobotoTypefaceManager.java
 */
public class RobotoTypefaceManager {

    private final static SparseArray<android.graphics.Typeface> mTypefaces = new SparseArray<>();

    public static android.graphics.Typeface obtainTypeface(Context context, int typefaceValue) throws IllegalArgumentException {
        android.graphics.Typeface typeface = mTypefaces.get(typefaceValue);
        if (typeface == null) {
            typeface = createTypeface(context, typefaceValue);
            mTypefaces.put(typefaceValue, typeface);
        }
        return typeface;
    }

    public static android.graphics.Typeface obtainTypeface(Context context, int fontFamily, int textWeight, int textStyle) throws IllegalArgumentException {
        int typefaceValue = getTypefaceValue(fontFamily, textWeight, textStyle);
        return obtainTypeface(context, typefaceValue);
    }

    private static int getTypefaceValue(int fontFamily, int textWeight, int textStyle) {
        int typeface;
        if (fontFamily == FontFamily.ROBOTO) {
            if (textWeight == TextWeight.NORMAL) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typeface.ROBOTO_REGULAR;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else if (textWeight == TextWeight.THIN) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typeface.ROBOTO_THIN;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else if (textWeight == TextWeight.LIGHT) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typeface.ROBOTO_LIGHT;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else if (textWeight == TextWeight.MEDIUM) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typeface.ROBOTO_MEDIUM;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else if (textWeight == TextWeight.BOLD) {
                switch (textStyle) {
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else {
                throw new IllegalArgumentException("`textWeight` attribute value " + textWeight +
                        " is not supported for this font family " + fontFamily);
            }
        } else if (fontFamily == FontFamily.ROBOTO_CONDENSED) {
            if (textWeight == TextWeight.NORMAL) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typeface.ROBOTO_CONDENSED_REGULAR;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else if (textWeight == TextWeight.LIGHT) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typeface.ROBOTO_CONDENSED_LIGHT;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else if (textWeight == TextWeight.BOLD) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typeface.ROBOTO_CONDENSED_BOLD;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else {
                throw new IllegalArgumentException("`textWeight` attribute value " + textWeight +
                        " is not supported for this font family " + fontFamily);
            }
        } else {
            throw new IllegalArgumentException("Unknown `fontFamily` attribute value " + fontFamily);
        }
        return typeface;
    }

    private static android.graphics.Typeface createTypeface(Context context, int typefaceValue) throws IllegalArgumentException {
        String typefacePath;
        switch (typefaceValue) {
            case Typeface.ROBOTO_THIN:
                typefacePath = "fonts/Roboto-Thin.ttf";
                break;
            case Typeface.ROBOTO_LIGHT:
                typefacePath = "fonts/Roboto-Light.ttf";
                break;
            case Typeface.ROBOTO_REGULAR:
                typefacePath = "fonts/Roboto-Regular.ttf";
                break;
            case Typeface.ROBOTO_MEDIUM:
                typefacePath = "fonts/Roboto-Medium.ttf";
                break;
            case Typeface.ROBOTO_CONDENSED_LIGHT:
                typefacePath = "fonts/RobotoCondensed-Light.ttf";
                break;
            case Typeface.ROBOTO_CONDENSED_REGULAR:
                typefacePath = "fonts/RobotoCondensed-Regular.ttf";
                break;
            case Typeface.ROBOTO_CONDENSED_BOLD:
                typefacePath = "fonts/RobotoCondensed-Bold.ttf";
                break;
            default:
                throw new IllegalArgumentException("Unknown `typeface` attribute value " + typefaceValue);
        }

        return android.graphics.Typeface.createFromAsset(context.getAssets(), typefacePath);
    }

    public class Typeface {
        public final static int ROBOTO_THIN = 0;
        public final static int ROBOTO_LIGHT = 2;
        public final static int ROBOTO_REGULAR = 4;
        public final static int ROBOTO_MEDIUM = 6;
        public final static int ROBOTO_CONDENSED_LIGHT = 12;
        public final static int ROBOTO_CONDENSED_REGULAR = 14;
        public final static int ROBOTO_CONDENSED_BOLD = 16;
    }

    public class FontFamily {
        public static final int ROBOTO = 0;
        public static final int ROBOTO_CONDENSED = 1;
    }

    public class TextWeight {
        public static final int NORMAL = 0;
        public static final int THIN = 1;
        public static final int LIGHT = 2;
        public static final int MEDIUM = 3;
        public static final int BOLD = 4;
    }

    public class TextStyle {
        public static final int NORMAL = 0;
    }

}