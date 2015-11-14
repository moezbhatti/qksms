package com.moez.QKSMS.common;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.SparseArray;

/**
 * Stripped down version of https://github.com/johnkil/Android-RobotoTextView/blob/master/robototextview/src/main/java/com/devspark/robototextview/util/RobotoTypefaceManager.java
 */
public class TypefaceManager {

    private final static SparseArray<android.graphics.Typeface> mTypefaces = new SparseArray<>();

    public static android.graphics.Typeface obtainTypeface(Context context, int typefaceValue) throws IllegalArgumentException {
        android.graphics.Typeface typeface = mTypefaces.get(typefaceValue);
        if (typeface == null) {
            typeface = createTypeface(typefaceValue);
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

    private static Typeface createTypeface(int typefaceValue) throws IllegalArgumentException {
        switch (typefaceValue) {
            case Typefaces.LATO_THIN:
            case Typefaces.ROBOTO_THIN:
                return Typeface.create("sans-serif-thin", Typeface.NORMAL);

            case Typefaces.LATO_LIGHT:
            case Typefaces.ROBOTO_LIGHT:
                return Typeface.create("sans-serif-light", Typeface.NORMAL);

            case Typefaces.LATO_REGULAR:
            case Typefaces.ROBOTO_REGULAR:
                return Typeface.create("sans-serif", Typeface.NORMAL);

            case Typefaces.LATO_MEDIUM:
            case Typefaces.ROBOTO_MEDIUM:
                String name = Build.VERSION.SDK_INT >= 21 ? "sans-serif-medium" : "sans-serif";
                int style = Build.VERSION.SDK_INT >= 21 ? Typeface.NORMAL : Typeface.BOLD;
                return Typeface.create(name, style);

            case Typefaces.ROBOTO_CONDENSED_LIGHT:
            case Typefaces.ROBOTO_CONDENSED_REGULAR:
                return Typeface.create("sans-serif-condensed", Typeface.NORMAL);

            case Typefaces.ROBOTO_CONDENSED_BOLD:
                return Typeface.create("sans-serif-condensed", Typeface.BOLD);

            default:
                throw new IllegalArgumentException("Unknown `typeface` attribute value " + typefaceValue);
        }
    }

    // The deprecated typefaces are no longer supported. Previously the fonts were stored as assets, but were
    // removed to reduce app size. Android does not have these deprecated fonts built in, but we still need
    // to keep these cases so that people with older versions of the app that did support these fonts had them
    // selected won't experience a crash when they upgrade to the new version that doesn't support them
    public class Typefaces {
        public final static int ROBOTO_THIN = 0;
        public final static int ROBOTO_LIGHT = 2;
        public final static int ROBOTO_REGULAR = 4;
        public final static int ROBOTO_MEDIUM = 6;
        @Deprecated public final static int ROBOTO_CONDENSED_LIGHT = 12;
        public final static int ROBOTO_CONDENSED_REGULAR = 14;
        public final static int ROBOTO_CONDENSED_BOLD = 16;
        @Deprecated public final static int LATO_THIN = 22;
        @Deprecated public final static int LATO_LIGHT = 24;
        @Deprecated public final static int LATO_REGULAR = 26;
        @Deprecated public final static int LATO_MEDIUM = 28;
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
