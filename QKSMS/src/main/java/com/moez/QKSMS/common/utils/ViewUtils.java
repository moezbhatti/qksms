package com.moez.QKSMS.common.utils;

import android.view.View;

public class ViewUtils {

    /**
     * Returns true if the given x,y coordinates falls within the view bounds.
     * @param view
     * @param x
     * @param y
     * @return
     */
    public static boolean isInBounds(View view, int x, int y) {

        int[] l = new int[2];
        view.getLocationOnScreen(l);
        int vx = l[0];
        int vy = l[1];
        int vw = view.getWidth();
        int vh = view.getHeight();

        return !(x < vx || x > vx + vw || y < vy || y > vy + vh);
    }
}
