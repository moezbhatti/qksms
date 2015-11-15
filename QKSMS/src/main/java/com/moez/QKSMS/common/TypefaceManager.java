package com.moez.QKSMS.common;

import android.graphics.Typeface;
import android.os.Build;
import android.util.SparseArray;

/**
 * Stripped down version of https://github.com/johnkil/Android-RobotoTextView/blob/master/robototextview/src/main/java/com/devspark/robototextview/util/RobotoTypefaceManager.java
 */
public class TypefaceManager {

    private final static SparseArray<android.graphics.Typeface> mTypefaces = new SparseArray<>();

    public static Typeface obtainTypeface(int fontFamily, int textWeight) throws IllegalArgumentException {
        int typefaceValue = Typefaces.ROBOTO_REGULAR;
        switch (fontFamily) {
            case FontFamily.ROBOTO:
                switch (textWeight) {
                    case TextWeight.NORMAL:
                        typefaceValue = Typefaces.ROBOTO_REGULAR;
                        break;
                    case TextWeight.THIN:
                        typefaceValue = Typefaces.ROBOTO_THIN;
                        break;
                    case TextWeight.LIGHT:
                        typefaceValue = Typefaces.ROBOTO_LIGHT;
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

        return obtainTypeface(typefaceValue);
    }

    public static Typeface obtainTypeface(int typefaceValue) throws IllegalArgumentException {
        android.graphics.Typeface typeface = mTypefaces.get(typefaceValue);
        if (typeface == null) {
            typeface = createTypeface(typefaceValue);
            mTypefaces.put(typefaceValue, typeface);
        }
        return typeface;
    }

    private static Typeface createTypeface(int typefaceValue) throws IllegalArgumentException {
        switch (typefaceValue) {
            case Typefaces.ROBOTO_THIN:
                return Typeface.create("sans-serif-thin", Typeface.NORMAL);

            case Typefaces.ROBOTO_LIGHT:
                return Typeface.create("sans-serif-light", Typeface.NORMAL);

            case Typefaces.ROBOTO_REGULAR:
                return Typeface.create("sans-serif", Typeface.NORMAL);

            case Typefaces.ROBOTO_MEDIUM:
                String name = Build.VERSION.SDK_INT >= 21 ? "sans-serif-medium" : "sans-serif";
                int style = Build.VERSION.SDK_INT >= 21 ? Typeface.NORMAL : Typeface.BOLD;
                return Typeface.create(name, style);

            case Typefaces.ROBOTO_CONDENSED_REGULAR:
                return Typeface.create("sans-serif-condensed", Typeface.NORMAL);

            case Typefaces.ROBOTO_CONDENSED_BOLD:
                return Typeface.create("sans-serif-condensed", Typeface.BOLD);

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
        public final static int DEFAULT_REGULAR = 6;
        public final static int DEFAULT_BOLD = 7;
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
