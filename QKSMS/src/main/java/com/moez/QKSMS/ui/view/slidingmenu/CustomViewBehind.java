package com.moez.QKSMS.ui.view.slidingmenu;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.moez.QKSMS.common.utils.Units;

public class CustomViewBehind extends ViewGroup {
    private static final String TAG = "CustomViewBehind";

    private View mContent;
    private int mWidthOffset;
    private boolean mChildrenEnabled;

    private float mScrollScale;
    private Drawable mShadowDrawable;
    private int mShadowWidth;

    public CustomViewBehind(Context context) {
        this(context, null);
    }

    public CustomViewBehind(Context context, AttributeSet attrs) {
        super(context, attrs);
        mShadowWidth = Units.dpToPx(getContext(), 8);
    }

    public void setWidthOffset(int i) {
        mWidthOffset = i;
        requestLayout();
    }

    public int getBehindWidth() {
        return mContent.getWidth();
    }

    public void setContent(View v) {
        if (mContent != null) {
            removeView(mContent);
        }
        mContent = v;
        addView(mContent);
    }

    public View getContent() {
        return mContent;
    }

    public void setChildrenEnabled(boolean enabled) {
        mChildrenEnabled = enabled;
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return !mChildrenEnabled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return !mChildrenEnabled;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int width = r - l;
        final int height = b - t;
        mContent.layout(0, 0, width - mWidthOffset, height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        setMeasuredDimension(width, height);
        final int contentWidth = getChildMeasureSpec(widthMeasureSpec, 0, width - mWidthOffset);
        final int contentHeight = getChildMeasureSpec(heightMeasureSpec, 0, height);
        mContent.measure(contentWidth, contentHeight);
    }

    public void setScrollScale(float scrollScale) {
        mScrollScale = scrollScale;
    }

    public void setShadowDrawable(Drawable shadow) {
        mShadowDrawable = shadow;
        invalidate();
    }

    public int getMenuPage(int page) {
        page = (page > 1) ? 2 : ((page < 1) ? 0 : page);
        if (page > 1) {
            return 0;
        } else {
            return page;
        }
    }

    public void scrollBehindTo(View content, int x, int y) {
        int vis = View.VISIBLE;
        if (x >= content.getLeft()) vis = View.INVISIBLE;
        scrollTo((int) ((x + getBehindWidth()) * mScrollScale), y);
        setVisibility(vis);
    }

    public int getMenuLeft(View content, int page) {
        switch (page) {
            case 0:
                return content.getLeft() - getBehindWidth();
            case 2:
                return content.getLeft();
        }
        return content.getLeft();
    }

    public int getAbsLeftBound(View content) {
        return content.getLeft() - getBehindWidth();
    }

    public int getAbsRightBound(View content) {
        return content.getLeft();
    }

    /**
     * Returns whether x is on a menu.
     *
     * @param content  content
     * @param x        the position of the touch
     */
    public boolean menuTouchInQuickReturn(View content, float x) {
        return x >= content.getLeft();
    }

    public boolean menuClosedSlideAllowed(float dx) {
        return dx > 0;
    }

    public boolean menuOpenSlideAllowed(float dx) {
        return dx < 0;
    }

    public void drawShadow(View content, Canvas canvas) {
        if (mShadowDrawable == null || mShadowWidth <= 0) return;
        int left = content.getLeft() - mShadowWidth;
        mShadowDrawable.setBounds(left, 0, left + mShadowWidth, getHeight());
        mShadowDrawable.draw(canvas);
    }

}
