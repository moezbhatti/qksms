package com.moez.QKSMS.ui.welcome;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class PagerScroller extends Scroller {

    private int mDuration = 500;

    public PagerScroller(Context context) {
        super(context);
    }

    public PagerScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public PagerScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }


    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, mDuration);
    }
}
