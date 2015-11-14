package com.moez.QKSMS.common;

import android.content.Context;
import android.graphics.Typeface;
import android.util.SparseArray;

/**
 * Stripped down version of https://github.com/johnkil/Android-RobotoTextView/blob/master/robototextview/src/main/java/com/devspark/robototextview/util/RobotoTypefaceManager.java
 */
public class TypefaceManager {

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
                        typeface = Typefaces.ROBOTO_REGULAR;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else if (textWeight == TextWeight.THIN) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typefaces.ROBOTO_THIN;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else if (textWeight == TextWeight.LIGHT) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typefaces.ROBOTO_LIGHT;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else if (textWeight == TextWeight.MEDIUM) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typefaces.ROBOTO_MEDIUM;
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
                        typeface = Typefaces.ROBOTO_CONDENSED_REGULAR;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else if (textWeight == TextWeight.LIGHT) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typefaces.ROBOTO_CONDENSED_LIGHT;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else if (textWeight == TextWeight.BOLD) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typefaces.ROBOTO_CONDENSED_BOLD;
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
       } else if (fontFamily == FontFamily.LATO) {
            if (textWeight == TextWeight.NORMAL) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typefaces.LATO_REGULAR;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else if (textWeight == TextWeight.THIN) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typefaces.LATO_THIN;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else if (textWeight == TextWeight.LIGHT) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typefaces.LATO_LIGHT;
                        break;
                    default:
                        throw new IllegalArgumentException("`textStyle` attribute value " + textStyle +
                                " is not supported for this fontFamily " + fontFamily +
                                " and textWeight " + textWeight);
                }
            } else if (textWeight == TextWeight.MEDIUM) {
                switch (textStyle) {
                    case TextStyle.NORMAL:
                        typeface = Typefaces.LATO_MEDIUM;
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
        } else {
            throw new IllegalArgumentException("Unknown `fontFamily` attribute value " + fontFamily);
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

            case Typefaces.LATO_THIN:
                return Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Hairline.ttf");

            case Typefaces.LATO_LIGHT:
                return Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Light.ttf");

            case Typefaces.LATO_REGULAR:
                return Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Regular.ttf");

            case Typefaces.LATO_MEDIUM:
                return Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Bold.ttf");

            default:
                throw new IllegalArgumentException("Unknown `typeface` attribute value " + typefaceValue);
        }
    }

    public class Typefaces {
        public final static int ROBOTO_THIN = 0;
        public final static int ROBOTO_LIGHT = 2;
        public final static int ROBOTO_REGULAR = 4;
        public final static int ROBOTO_MEDIUM = 6;
        public final static int ROBOTO_CONDENSED_LIGHT = 12;
        public final static int ROBOTO_CONDENSED_REGULAR = 14;
        public final static int ROBOTO_CONDENSED_BOLD = 16;
        public final static int LATO_THIN = 22;
        public final static int LATO_LIGHT = 24;
        public final static int LATO_REGULAR = 26;
        public final static int LATO_MEDIUM = 28;
    }

    public class FontFamily {
        public static final int ROBOTO = 0;
        public static final int ROBOTO_CONDENSED = 1;
        public static final int LATO = 2;
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
