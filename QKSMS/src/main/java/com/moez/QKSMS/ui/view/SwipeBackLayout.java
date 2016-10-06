package com.moez.QKSMS.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.moez.QKSMS.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Modified version of https://github.com/ikew0ng/SwipeBackLayout
 */
public class SwipeBackLayout extends FrameLayout {
    private final String TAG = "SwipeBackLayout";

    public interface ScrollChangedListener {
        void onScrollChanged(float scrollPercent);
    }

    private static final int MIN_FLING_VELOCITY = 400; // dips per second
    private static final int DEFAULT_SCRIM_COLOR = 0x99000000;
    private static final int FULL_ALPHA = 255;
    private static final float DEFAULT_SCROLL_THRESHOLD = 0.3f;
    private static final int OVERSCROLL_DISTANCE = 10;

    public static final int EDGE_LEFT = ViewDragHelper.EDGE_LEFT;
    public static final int EDGE_BOTTOM = ViewDragHelper.EDGE_BOTTOM;
    public static final int EDGE_ALL = EDGE_LEFT | EDGE_BOTTOM;

    public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;
    public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;
    public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;


    private static final int[] EDGE_FLAGS = {
            EDGE_LEFT, EDGE_BOTTOM, EDGE_ALL
    };

    private int mEdgeFlag;

    /**
     * Threshold of scroll, we will close the activity, when scrollPercent over
     * this value;
     */
    private float mScrollThreshold = DEFAULT_SCROLL_THRESHOLD;

    private Activity mActivity;

    private boolean mEnable = true;

    private View mContentView;

    private ViewDragHelper mDragHelper;

    private float mScrollPercent;

    private int mContentLeft;
    private int mContentTop;

    /**
     * The set of listeners to be sent events through.
     */
    private List<SwipeListener> mListeners;
    private ScrollChangedListener mScrollChangedListener;

    private Drawable mShadowLeft;
    private Drawable mShadowBottom;

    private float mScrimOpacity;
    private int mScrimColor = DEFAULT_SCRIM_COLOR;

    private boolean mInLayout;
    private Rect mTmpRect = new Rect();

    /**
     * Edge being dragged
     */
    private int mTrackingEdge = EDGE_LEFT;

    public SwipeBackLayout(Context context) {
        this(context, null);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeBackLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mDragHelper = ViewDragHelper.create(this, 0.2f, new ViewDragCallback());

        int shadowLeft = R.drawable.shadow_left;
        int shadowBottom = R.drawable.shadow_bottom;
        setShadow(shadowLeft, EDGE_LEFT);
        setShadow(shadowBottom, EDGE_BOTTOM);
        final float density = getResources().getDisplayMetrics().density;
        final float minVel = MIN_FLING_VELOCITY * density;
        mDragHelper.setMinVelocity(minVel);
    }

    /**
     * Set up contentView which will be moved by user gesture
     *
     * @param view
     */
    private void setContentView(View view) {
        mContentView = view;
    }

    public void setEnableGesture(boolean enable) {
        mEnable = enable;
    }

    /**
     * Enable edge tracking for the selected edges of the parent view. The
     * callback's
     * {@link android.support.v4.widget.ViewDragHelper.Callback#onEdgeTouched(int, int)}
     * and
     * {@link android.support.v4.widget.ViewDragHelper.Callback#onEdgeDragStarted(int, int)}
     * methods will only be invoked for edges for which edge tracking has been
     * enabled.
     *
     * @param edgeFlags Combination of edge flags describing the edges to watch
     * @see #EDGE_LEFT
     * @see #EDGE_BOTTOM
     */
    public void setEdgeTrackingEnabled(int edgeFlags) {
        mEdgeFlag = edgeFlags;
        mDragHelper.setEdgeTrackingEnabled(mEdgeFlag);
    }

    /**
     * Set a color to use for the scrim that obscures primary content while a
     * drawer is open.
     *
     * @param color Color to use in 0xAARRGGBB format.
     */
    public void setScrimColor(int color) {
        mScrimColor = color;
        invalidate();
    }

    /**
     * Register a callback to be invoked when a swipe event is sent to this
     * view.
     *
     * @param listener the swipe listener to attach to this view
     * @deprecated use {@link #addSwipeListener} instead
     */
    @Deprecated
    public void setSwipeListener(SwipeListener listener) {
        addSwipeListener(listener);
    }

    /**
     * Add a callback to be invoked when a swipe event is sent to this view.
     *
     * @param listener the swipe listener to attach to this view
     */
    public void addSwipeListener(SwipeListener listener) {
        if (mListeners == null) {
            mListeners = new ArrayList<SwipeListener>();
        }
        mListeners.add(listener);
    }

    /**
     * Removes a listener from the set of listeners
     *
     * @param listener
     */
    public void removeSwipeListener(SwipeListener listener) {
        if (mListeners == null) {
            return;
        }
        mListeners.remove(listener);
    }

    public void setScrollChangedListener(ScrollChangedListener listener) {
        mScrollChangedListener = listener;
    }

    public interface SwipeListener {
        /**
         * Invoke when state change
         *
         * @param state         flag to describe scroll state
         * @param scrollPercent scroll percent of this view
         * @see #STATE_IDLE
         * @see #STATE_DRAGGING
         * @see #STATE_SETTLING
         */
        void onScrollStateChange(int state, float scrollPercent);

        /**
         * Invoke when edge touched
         *
         * @param edgeFlag edge flag describing the edge being touched
         * @see #EDGE_LEFT
         * @see #EDGE_BOTTOM
         */
        void onEdgeTouch(int edgeFlag);

        /**
         * Invoke when scroll percent over the threshold for the first time
         */
        void onScrollOverThreshold();
    }

    /**
     * Set scroll threshold, we will close the activity, when scrollPercent over
     * this value
     *
     * @param threshold
     */
    public void setScrollThresHold(float threshold) {
        if (threshold >= 1.0f || threshold <= 0) {
            throw new IllegalArgumentException("Threshold value should be between 0 and 1.0");
        }
        mScrollThreshold = threshold;
    }

    /**
     * Set a drawable used for edge shadow.
     *
     * @param shadow    Drawable to use
     * @param edgeFlags Combination of edge flags describing the edge to set
     * @see #EDGE_LEFT
     * @see #EDGE_BOTTOM
     */
    public void setShadow(Drawable shadow, int edgeFlag) {
        if ((edgeFlag & EDGE_LEFT) != 0) {
            mShadowLeft = shadow;
        } else if ((edgeFlag & EDGE_BOTTOM) != 0) {
            mShadowBottom = shadow;
        }
        invalidate();
    }

    /**
     * Set a drawable used for edge shadow.
     *
     * @param resId     Resource of drawable to use
     * @param edgeFlags Combination of edge flags describing the edge to set
     * @see #EDGE_LEFT
     * @see #EDGE_BOTTOM
     */
    public void setShadow(int resId, int edgeFlag) {
        setShadow(getResources().getDrawable(resId), edgeFlag);
    }

    /**
     * Scroll out contentView and finish the activity
     */
    public void scrollToFinishActivity() {
        final int childWidth = mContentView.getWidth();
        final int childHeight = mContentView.getHeight();

        int left = 0, top = 0;
        if ((mEdgeFlag & EDGE_LEFT) != 0) {
            left = childWidth + mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE;
            mTrackingEdge = EDGE_LEFT;
        } else if ((mEdgeFlag & EDGE_BOTTOM) != 0) {
            top = -childHeight - mShadowBottom.getIntrinsicHeight() - OVERSCROLL_DISTANCE;
            mTrackingEdge = EDGE_BOTTOM;
        }

        mDragHelper.smoothSlideViewTo(mContentView, left, top);
        invalidate();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!mEnable) {
            return false;
        }
        try {
            return mDragHelper.shouldInterceptTouchEvent(event);
        } catch (ArrayIndexOutOfBoundsException e) {
            // FIXME: handle exception
            // issues #9
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mEnable) {
            return false;
        }
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mInLayout = true;
        if (mContentView != null)
            mContentView.layout(mContentLeft, mContentTop,
                    mContentLeft + mContentView.getMeasuredWidth(),
                    mContentTop + mContentView.getMeasuredHeight());
        mInLayout = false;
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        final boolean drawContent = child == mContentView;

        boolean ret = super.drawChild(canvas, child, drawingTime);
        if (mScrimOpacity > 0 && drawContent && mDragHelper.getViewDragState() != ViewDragHelper.STATE_IDLE) {
            drawShadow(canvas, child);
            drawScrim(canvas, child);
        }
        return ret;
    }

    private void drawScrim(Canvas canvas, View child) {
        final int baseAlpha = (mScrimColor & 0xff000000) >>> 24;
        final int alpha = (int) (baseAlpha * mScrimOpacity);
        final int color = alpha << 24 | (mScrimColor & 0xffffff);

        if ((mTrackingEdge & EDGE_LEFT) != 0) {
            canvas.clipRect(0, 0, child.getLeft(), getHeight());
        } else if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
            canvas.clipRect(child.getLeft(), child.getBottom(), getRight(), getHeight());
        }
        canvas.drawColor(color);
    }

    private void drawShadow(Canvas canvas, View child) {
        final Rect childRect = mTmpRect;
        child.getHitRect(childRect);

        if ((mEdgeFlag & EDGE_LEFT) != 0) {
            mShadowLeft.setBounds(childRect.left - mShadowLeft.getIntrinsicWidth(), childRect.top,
                    childRect.left, childRect.bottom);
            mShadowLeft.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowLeft.draw(canvas);
        }

        if ((mEdgeFlag & EDGE_BOTTOM) != 0) {
            mShadowBottom.setBounds(childRect.left, childRect.bottom, childRect.right,
                    childRect.bottom + mShadowBottom.getIntrinsicHeight());
            mShadowBottom.setAlpha((int) (mScrimOpacity * FULL_ALPHA));
            mShadowBottom.draw(canvas);
        }
    }

    public void attachToActivity(Activity activity) {
        mActivity = activity;
        TypedArray a = activity.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.windowBackground
        });
        int background = a.getResourceId(0, 0);
        a.recycle();

        ViewGroup decor = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup decorChild = (ViewGroup) decor.getChildAt(0);
        decorChild.setBackgroundResource(background);
        decor.removeView(decorChild);
        addView(decorChild);
        setContentView(decorChild);
        decor.addView(this);
    }

    @Override
    public void computeScroll() {
        mScrimOpacity = mScrollPercent < 0.8f ? 0.4f : (1f - mScrollPercent) * 2f;
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private class ViewDragCallback extends ViewDragHelper.Callback {
        private boolean mIsScrollOverValid;

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            super.onEdgeDragStarted(edgeFlags, pointerId);
        }

        @Override
        public boolean tryCaptureView(View view, int i) {
            mTrackingEdge = EDGE_LEFT;
            if (mListeners != null && !mListeners.isEmpty()) {
                for (SwipeListener listener : mListeners) {
                    listener.onEdgeTouch(mTrackingEdge);
                }
            }
            mIsScrollOverValid = true;
            boolean directionCheck = false;
            if (mEdgeFlag == EDGE_LEFT) {
                directionCheck = !mDragHelper.checkTouchSlop(ViewDragHelper.DIRECTION_VERTICAL, i);
            } else if (mEdgeFlag == EDGE_BOTTOM) {
                directionCheck = !mDragHelper.checkTouchSlop(ViewDragHelper.DIRECTION_HORIZONTAL, i);
            } else if (mEdgeFlag == EDGE_ALL) {
                directionCheck = true;
            }
            return directionCheck;
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return mEdgeFlag & (EDGE_LEFT);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return mEdgeFlag & EDGE_BOTTOM;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            if ((mTrackingEdge & EDGE_LEFT) != 0) {
                mScrollPercent = Math.abs((float) left / (mContentView.getWidth() + mShadowLeft.getIntrinsicWidth()));
            } else if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
                mScrollPercent = Math.abs((float) top / (mContentView.getHeight() + mShadowBottom.getIntrinsicHeight()));
            }

            if (mScrollChangedListener != null) {
                mScrollChangedListener.onScrollChanged(mScrollPercent);
            }

            mContentLeft = left;
            mContentTop = top;
            invalidate();
            if (mScrollPercent < mScrollThreshold && !mIsScrollOverValid) {
                mIsScrollOverValid = true;
            }
            if (mListeners != null && !mListeners.isEmpty()
                    && mDragHelper.getViewDragState() == STATE_DRAGGING
                    && mScrollPercent >= mScrollThreshold && mIsScrollOverValid) {
                mIsScrollOverValid = false;
                for (SwipeListener listener : mListeners) {
                    listener.onScrollOverThreshold();
                }
            }

            if (mScrollPercent >= 1) {
                if (!mActivity.isFinishing()) {
                    mActivity.finish();
                    mActivity.overridePendingTransition(0, 0);
                }
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            final int childWidth = releasedChild.getWidth();
            final int childHeight = releasedChild.getHeight();

            int left = 0, top = 0;
            if ((mTrackingEdge & EDGE_LEFT) != 0) {
                left = xvel > 0 || xvel == 0 && mScrollPercent > mScrollThreshold ? childWidth
                        + mShadowLeft.getIntrinsicWidth() + OVERSCROLL_DISTANCE : 0;
            } else if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
                top = yvel < 0 || yvel == 0 && mScrollPercent > mScrollThreshold ? -(childHeight
                        + mShadowBottom.getIntrinsicHeight() + OVERSCROLL_DISTANCE) : 0;
            }

            mDragHelper.settleCapturedViewAt(left, top);
            invalidate();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int ret = 0;
            if ((mTrackingEdge & EDGE_LEFT) != 0) {
                ret = Math.min(child.getWidth(), Math.max(left, 0));
            }
            return ret;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            int ret = 0;
            if ((mTrackingEdge & EDGE_BOTTOM) != 0) {
                ret = Math.min(0, Math.max(top, -child.getHeight()));
            }
            return ret;
        }

        @Override
        public void onViewDragStateChanged(int state) {
            super.onViewDragStateChanged(state);
            if (mListeners != null && !mListeners.isEmpty()) {
                for (SwipeListener listener : mListeners) {
                    listener.onScrollStateChange(state, mScrollPercent);
                }
            }
        }
    }
}