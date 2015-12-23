package com.moez.QKSMS.ui.view.slidingmenu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.utils.Units;
import com.moez.QKSMS.ui.view.slidingmenu.CustomViewAbove.OnPageChangeListener;

/**
 * Stripped down version of Jeremy Feinstein's SlidingMenu library
 */
public class SlidingMenu extends RelativeLayout {
    private static final String TAG = "SlidingMenu";

    private boolean mActionbarOverlay = false;

    private CustomViewAbove mViewAbove;
    private CustomViewBehind mViewBehind;

    private OnOpenListener mOpenListener;
    private OnOpenedListener mOpenedListener;
    private OnCloseListener mCloseListener;
    private OnClosedListener mClosedListener;

    /**
     * The listener interface for receiving onOpen events.
     * The class that is interested in processing a onOpen
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addOnOpenListener<code> method. When
     * the onOpen event occurs, that object's appropriate
     * method is invoked
     */
    public interface OnOpenListener {

        /**
         * On open.
         */
        void onOpen();
    }

    /**
     * The listener interface for receiving onOpened events.
     * The class that is interested in processing a onOpened
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addOnOpenedListener<code> method. When
     * the onOpened event occurs, that object's appropriate
     * method is invoked.
     *
     * @see OnOpenedEvent
     */
    public interface OnOpenedListener {

        /**
         * On opened.
         */
        void onOpened();
    }

    /**
     * The listener interface for receiving onClose events.
     * The class that is interested in processing a onClose
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addOnCloseListener<code> method. When
     * the onClose event occurs, that object's appropriate
     * method is invoked.
     *
     * @see OnCloseEvent
     */
    public interface OnCloseListener {

        /**
         * On close.
         */
        void onClose();
    }

    /**
     * The listener interface for receiving onClosed events.
     * The class that is interested in processing a onClosed
     * event implements this interface, and the object created
     * with that class is registered with a component using the
     * component's <code>addOnClosedListener<code> method. When
     * the onClosed event occurs, that object's appropriate
     * method is invoked.
     *
     * @see OnClosedEvent
     */
    public interface OnClosedListener {

        /**
         * On closed.
         */
        void onClosed();
    }

    /**
     * Instantiates a new SlidingMenu.
     *
     * @param context the associated Context
     */
    public SlidingMenu(Context context) {
        this(context, null);
    }

    /**
     * Instantiates a new SlidingMenu.
     *
     * @param context the associated Context
     * @param attrs   the attrs
     */
    public SlidingMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Instantiates a new SlidingMenu.
     *
     * @param context  the associated Context
     * @param attrs    the attrs
     * @param defStyle the def style
     */
    public SlidingMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        LayoutParams behindParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mViewBehind = new CustomViewBehind(context);
        addView(mViewBehind, behindParams);
        LayoutParams aboveParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mViewAbove = new CustomViewAbove(context);
        addView(mViewAbove, aboveParams);
        mViewBehind.setScrollScale(0.5f);
        mViewBehind.setShadowDrawable(ContextCompat.getDrawable(getContext(), R.drawable.shadow_slidingmenu));
        mViewAbove.setCustomViewBehind(mViewBehind);
        mViewAbove.setOnPageChangeListener(new OnPageChangeListener() {
            public static final int POSITION_OPEN = 0;
            public static final int POSITION_CLOSE = 1;

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position, boolean anim) {
                if (position == POSITION_OPEN && mOpenListener != null && anim) {
                    mOpenListener.onOpen();
                } else if (position == POSITION_OPEN && mOpenedListener != null) {
                    mOpenedListener.onOpened();
                } else if (position == POSITION_CLOSE && mCloseListener != null && anim) {
                    mCloseListener.onClose();
                } else if (position == POSITION_CLOSE && mClosedListener != null) {
                    mClosedListener.onClosed();
                }
            }
        });

    }

    /**
     * Set the above view content from a layout resource. The resource will be inflated, adding all top-level views
     * to the above view.
     *
     * @param res the new content
     */
    public void setContent() {
        mViewAbove.setContent(LayoutInflater.from(getContext()).inflate(R.layout.content_frame, null));
    }

    /**
     * Retrieves the current content.
     *
     * @return the current content
     */
    public View getContent() {
        return mViewAbove.getContent();
    }

    /**
     * Set the behind view (menu) content from a layout resource. The resource will be inflated, adding all top-level views
     * to the behind view.
     *
     * @param res the new content
     */
    public void setMenu() {
        mViewBehind.setContent(LayoutInflater.from(getContext()).inflate(R.layout.menu_frame, null));
    }

    /**
     * Retrieves the main menu.
     *
     * @return the main menu
     */
    public View getMenu() {
        return mViewBehind.getContent();
    }

    /**
     * Opens the menu and shows the menu view.
     */
    public void showMenu() {
        showMenu(true);
    }

    /**
     * Opens the menu and shows the menu view.
     *
     * @param animate true to animate the transition, false to ignore animation
     */
    public void showMenu(boolean animate) {
        mViewAbove.setCurrentItem(0, animate, true);
    }

    /**
     * Closes the menu and shows the above view.
     */
    public void showContent() {
        showContent(true);
    }

    /**
     * Closes the menu and shows the above view.
     *
     * @param animate true to animate the transition, false to ignore animation
     */
    public void showContent(boolean animate) {
        mViewAbove.setCurrentItem(1, animate, true);
    }

    /**
     * Toggle the SlidingMenu. If it is open, it will be closed, and vice versa.
     */
    public void toggle() {
        toggle(true);
    }

    /**
     * Toggle the SlidingMenu. If it is open, it will be closed, and vice versa.
     *
     * @param animate true to animate the transition, false to ignore animation
     */
    public void toggle(boolean animate) {
        if (isMenuShowing()) {
            showContent(animate);
        } else {
            showMenu(animate);
        }
    }

    /**
     * Checks if is the behind view showing.
     *
     * @return Whether or not the behind view is showing
     */
    public boolean isMenuShowing() {
        return mViewAbove.getCurrentItem() == 0 || mViewAbove.getCurrentItem() == 2;
    }

    /**
     * Sets the behind offset.
     *
     * @param i The margin, in pixels, on the right of the screen that the behind view scrolls to.
     */
    public void setBehindOffset(int i) {
        //		RelativeLayout.LayoutParams params = ((RelativeLayout.LayoutParams)mViewBehind.getLayoutParams());
        //		int bottom = params.bottomMargin;
        //		int top = params.topMargin;
        //		int left = params.leftMargin;
        //		params.setMargins(left, top, i, bottom);
        mViewBehind.setWidthOffset(i);
    }

    /**
     * Sets the OnOpenListener. {@link OnOpenListener#onOpen() OnOpenListener.onOpen()} will be called when the SlidingMenu is opened
     *
     * @param listener the new OnOpenListener
     */
    public void setOnOpenListener(OnOpenListener listener) {
        mOpenListener = listener;
    }


    /**
     * Sets the OnCloseListener. {@link OnCloseListener#onClose() OnCloseListener.onClose()} will be called when any one of the SlidingMenu is closed
     *
     * @param listener the new setOnCloseListener
     */
    public void setOnCloseListener(OnCloseListener listener) {
        mCloseListener = listener;
    }

    /**
     * Sets the OnOpenedListener. {@link OnOpenedListener#onOpened() OnOpenedListener.onOpened()} will be called after the SlidingMenu is opened
     *
     * @param listener the new OnOpenedListener
     */
    public void setOnOpenedListener(OnOpenedListener listener) {
        mOpenedListener = listener;
        mViewAbove.setOnOpenedListener(listener);
    }

    /**
     * Sets the OnClosedListener. {@link OnClosedListener#onClosed() OnClosedListener.onClosed()} will be called after the SlidingMenu is closed
     *
     * @param listener the new OnClosedListener
     */
    public void setOnClosedListener(OnClosedListener listener) {
        mClosedListener = listener;
        mViewAbove.setOnClosedListener(listener);
    }

    public static class SavedState extends BaseSavedState {

        private final int mItem;

        public SavedState(Parcelable superState, int item) {
            super(superState);
            mItem = item;
        }

        private SavedState(Parcel in) {
            super(in);
            mItem = in.readInt();
        }

        public int getItem() {
            return mItem;
        }

        /* (non-Javadoc)
         * @see android.view.AbsSavedState#writeToParcel(android.os.Parcel, int)
         */
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mItem);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

    }

    /* (non-Javadoc)
     * @see android.view.View#onSaveInstanceState()
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState, mViewAbove.getCurrentItem());
        return ss;
    }

    /* (non-Javadoc)
     * @see android.view.View#onRestoreInstanceState(android.os.Parcelable)
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        mViewAbove.setCurrentItem(ss.getItem());
    }

    /* (non-Javadoc)
     * @see android.view.ViewGroup#fitSystemWindows(android.graphics.Rect)
     */
    @SuppressLint("NewApi")
    @Override
    protected boolean fitSystemWindows(Rect insets) {
        int leftPadding = insets.left;
        int rightPadding = insets.right;
        int topPadding = insets.top;
        int bottomPadding = insets.bottom;
        if (!mActionbarOverlay) {
            setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
        }
        return true;
    }

    public void manageLayers(float percentOpen) {
        if (Build.VERSION.SDK_INT < 11) return;

        boolean layer = percentOpen > 0.0f && percentOpen < 1.0f;
        final int layerType = layer ? View.LAYER_TYPE_HARDWARE : View.LAYER_TYPE_NONE;

        if (layerType != getContent().getLayerType()) {
            getHandler().post(() -> {
                getContent().setLayerType(layerType, null);
                getMenu().setLayerType(layerType, null);
            });
        }
    }

}
