package com.moez.QKSMS.common;

import android.animation.TypeEvaluator;

/**
 * Evaluator used for animating between colors in the CIE-LCh color space
 * <p>
 * For reading see www.stuartdenman.com/improved-color-blending
 */
public class CIELChEvaluator implements TypeEvaluator<Integer> {

    /**
     * Converting RGB to CIE-LCh is expensive, so since we're only going to be using
     * an instance of this evaluator to evaluate between two colors, we'll calculate
     * the CIE-LCh values during construction rather than during frames in the
     * animation
     */
    private final ColorCIELCh mStartColor;
    private final ColorCIELCh mEndColor;

    public CIELChEvaluator(int startColor, int endColor) {
        mStartColor = convertRgbToCIELCH(startColor);
        mEndColor = convertRgbToCIELCH(endColor);
    }

    public Integer evaluate(float fraction) {
        return evaluate(fraction, 0, 0);
    }

    @Override
    public Integer evaluate(float fraction, Integer ignored, Integer ignored2) {

        // CIELCH to CIELAB
        double L = mStartColor.L * (1 - fraction) + mEndColor.L * fraction;
        double C = mStartColor.C * (1 - fraction) + mEndColor.C * fraction;
        double H = mStartColor.H * (1 - fraction) + mEndColor.H * fraction;

        double a = Math.cos(Math.toRadians(H)) * C;
        double b = Math.sin(Math.toRadians(H)) * C;

        // CIELAB to XYZ
        double var_Y = (L + 16) / 116.0;
        double var_X = a / 500 + var_Y;
        double var_Z = var_Y - b / 200.0;

        var_Y = Math.pow(var_Y, 3) > 0.008856 ? Math.pow(var_Y, 3) : (var_Y - 16 / 116.0) / 7.787;
        var_X = Math.pow(var_X, 3) > 0.008856 ? Math.pow(var_X, 3) : (var_X - 16 / 116.0) / 7.787;
        var_Z = Math.pow(var_Z, 3) > 0.008856 ? Math.pow(var_Z, 3) : (var_Z - 16 / 116.0) / 7.787;

        double X = 95.047 * var_X;
        double Y = 100.000 * var_Y;
        double Z = 108.883 * var_Z;


        // XYZ TO RGB
        double var_X2 = X / 100.0;
        double var_Y2 = Y / 100.0;
        double var_Z2 = Z / 100.0;

        double var_R = var_X2 * 3.2406 + var_Y2 * -1.5372 + var_Z2 * -0.4986;
        double var_G = var_X2 * -0.9689 + var_Y2 * 1.8758 + var_Z2 * 0.0415;
        double var_B = var_X2 * 0.0557 + var_Y2 * -0.2040 + var_Z2 * 1.0570;

        var_R = var_R > 0.0031308 ? 1.055 * Math.pow(var_R, 1 / 2.4) - 0.055 : 12.92 * var_R;
        var_G = var_G > 0.0031308 ? 1.055 * Math.pow(var_G, 1 / 2.4) - 0.055 : 12.92 * var_G;
        var_B = var_B > 0.0031308 ? 1.055 * Math.pow(var_B, 1 / 2.4) - 0.055 : 12.92 * var_B;

        double R = var_R * 255;
        double G = var_G * 255;
        double B = var_B * 255;

        int red = (int) Math.round(R);
        int green = (int) Math.round(G);
        int blue = (int) Math.round(B);

        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));
        blue = Math.min(255, Math.max(0, blue));

        return (0xff << 24) | (red << 16) | (green << 8) | (blue << 0);
    }

    private ColorCIELCh convertRgbToCIELCH(int rgb) {

        // RGB TO XYZ
        int r = 0xff & (rgb >> 16);
        int g = 0xff & (rgb >> 8);
        int b = 0xff & (rgb >> 0);

        double var_R = r / 255.0;
        double var_G = g / 255.0;
        double var_B = b / 255.0;

        var_R = var_R > 0.04045 ? Math.pow((var_R + 0.055) / 1.055, 2.4) : var_R / 12.92;
        var_G = var_G > 0.04045 ? Math.pow((var_G + 0.055) / 1.055, 2.4) : var_G / 12.92;
        var_B = var_B > 0.04045 ? Math.pow((var_B + 0.055) / 1.055, 2.4) : var_B / 12.92;

        var_R = var_R * 100;
        var_G = var_G * 100;
        var_B = var_B * 100;

        double X = var_R * 0.4124 + var_G * 0.3576 + var_B * 0.1805;
        double Y = var_R * 0.2126 + var_G * 0.7152 + var_B * 0.0722;
        double Z = var_R * 0.0193 + var_G * 0.1192 + var_B * 0.9505;


        // XYZ TO CIELAB
        double var_X = X / 95.047;
        double var_Y = Y / 100.000;
        double var_Z = Z / 108.883;

        var_X = var_X > 0.008856 ? Math.pow(var_X, 1 / 3.0) : (7.787 * var_X) + (16 / 116.0);
        var_Y = var_Y > 0.008856 ? Math.pow(var_Y, 1 / 3.0) : (7.787 * var_Y) + (16 / 116.0);
        var_Z = var_Z > 0.008856 ? Math.pow(var_Z, 1 / 3.0) : (7.787 * var_Z) + (16 / 116.0);

        double CIELAB_L = (116 * var_Y) - 16;
        double CIELAB_A = 500 * (var_X - var_Y);
        double CIELAB_B = 200 * (var_Y - var_Z);


        // CIELAB TO CIELCH
        double var_H = Math.atan2(CIELAB_B, CIELAB_A);
        var_H = var_H > 0 ? (var_H / Math.PI) * 180.0 : 360 - Math.toDegrees(Math.abs(var_H));

        double C = Math.hypot(CIELAB_A, CIELAB_B);
        double H = var_H;


        return new ColorCIELCh(CIELAB_L, C, H);
    }

    private static class ColorCIELCh {

        public final double L, C, H;

        public ColorCIELCh(double l, double C, double H) {
            L = l;
            this.C = C;
            this.H = H;
        }

        @Override
        public String toString() {
            return "{L:" + L + ", C:" + C + ", H:" + H + "}";
        }
    }
}
