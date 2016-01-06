package com.moez.QKSMS.common.utils;

import android.graphics.Color;

public class ColorUtils {
    private static final String TAG = "ColorUtils";

    public static int lighten(int color) {
        double r = Color.red(color);
        double g = Color.green(color);
        double b = Color.blue(color);

        r *= 1.1;
        g *= 1.1;
        b *= 1.1;

        double threshold = 255.999;
        double max = Math.max(r, Math.max(g, b));

        if (max > threshold) {
            double total = r + g + b;
            if (total >= 3 * threshold)
                return Color.WHITE;

            double x = (3 * threshold - total) / (3 * max - total);
            double gray = threshold - x * max;

            r = gray + x * r;
            g = gray + x * g;
            b = gray + x * b;
        }

        return Color.argb(255, (int) r, (int) g, (int) b);
    }

    public static int darken(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.85f;
        color = Color.HSVToColor(hsv);
        return color;
    }
}
