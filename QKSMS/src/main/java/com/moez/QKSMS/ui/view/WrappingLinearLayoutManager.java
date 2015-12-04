package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import com.moez.QKSMS.BuildConfig;

/**
 * {@link android.support.v7.widget.LinearLayoutManager} which wraps its content. Note that this class will always
 * wrap the content regardless of {@link android.support.v7.widget.RecyclerView} layout parameters.
 * <p/>
 * Now it's impossible to run add/remove animations with child views which have arbitrary dimensions (height for
 * VERTICAL orientation and width for HORIZONTAL). However if child views have fixed dimensions
 * {@link #setChildSize(int)} method might be used to let the layout manager know how big they are going to be.
 * If animations are not used at all then a normal measuring procedure will run and child views will be measured during
 * the measure pass.
 */
public class WrappingLinearLayoutManager extends android.support.v7.widget.LinearLayoutManager {

    private static final int CHILD_WIDTH = 0;
    private static final int CHILD_HEIGHT = 1;
    private static final int DEFAULT_CHILD_SIZE = 100;

    private final int[] childDimensions = new int[2];
    private final RecyclerView view;

    private int mFooterSize;
    private int childSize = DEFAULT_CHILD_SIZE;

    @SuppressWarnings("UnusedDeclaration")
    public WrappingLinearLayoutManager(Context context) {
        super(context);
        this.view = null;
    }

    @SuppressWarnings("UnusedDeclaration")
    public WrappingLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        this.view = null;
    }

    @SuppressWarnings("UnusedDeclaration")
    public WrappingLinearLayoutManager(RecyclerView view) {
        super(view.getContext());
        this.view = view;
    }

    @SuppressWarnings("UnusedDeclaration")
    public WrappingLinearLayoutManager(RecyclerView view, int orientation, boolean reverseLayout) {
        super(view.getContext(), orientation, reverseLayout);
        this.view = view;
    }

    public static int makeUnspecifiedSpec() {
        return View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
    }

    @Override
    public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        final int widthMode = View.MeasureSpec.getMode(widthSpec);
        final int heightMode = View.MeasureSpec.getMode(heightSpec);

        final int widthSize = View.MeasureSpec.getSize(widthSpec);
        final int heightSize = View.MeasureSpec.getSize(heightSpec);

        final boolean exactWidth = widthMode == View.MeasureSpec.EXACTLY;
        final boolean exactHeight = heightMode == View.MeasureSpec.EXACTLY;

        final int unspecified = makeUnspecifiedSpec();

        if (exactWidth && exactHeight) {
            // in case of exact calculations for both dimensions let's use default "onMeasure" implementation
            super.onMeasure(recycler, state, widthSpec, heightSpec);
            return;
        }

        final boolean vertical = getOrientation() == VERTICAL;

        initChildDimensions(widthSize, heightSize, vertical);

        int height = 0;

        // it's possible to getConversation scrap views in recycler which are bound to old (invalid) adapter entities. This
        // happens because their invalidation happens after "onMeasure" method. As a workaround let's clear the
        // recycler now (it should not cause any performance issues while scrolling as "onMeasure" is never
        // called whiles scrolling)
        recycler.clear();

        final int stateItemCount = state.getItemCount();
        final int adapterItemCount = getItemCount();
        // adapter always contains actual data while state might contain old data (f.e. data before the animation is
        // done). As we want to measure the view with actual data we must use data from the adapter and not from  the
        // state
        for (int i = 0; i < adapterItemCount; i++) {
            if (i < stateItemCount) {
                // we should not exceed state count, otherwise we'll getConversation IndexOutOfBoundsException. For such items
                // we will use previously calculated dimensions
                measureChild(recycler, i, widthSpec, unspecified, childDimensions);
            } else {
                logMeasureWarning(i);
            }
            height += childDimensions[CHILD_HEIGHT];
            if (height >= heightSize) {
                break;
            }
        }

        final boolean fit = (vertical && height < heightSize);
        if (fit) {
            // we really should wrap the contents of the view, let's do it

            if (exactHeight) {
                height = heightSize;
            } else {
                height += getPaddingTop() + getPaddingBottom();
            }

            setMeasuredDimension(widthSize, height + mFooterSize);
        } else {
            // if calculated height/width exceeds requested height/width let's use default "onMeasure" implementation
            super.onMeasure(recycler, state, widthSpec, heightSpec);
        }
    }

    private void logMeasureWarning(int child) {
        if (BuildConfig.DEBUG) {
            Log.w("WrappingLinearLayoutManager", "Can't measure child #" + child + ", previously used dimensions will be reused." +
                    "To remove this message either use #setChildSize() method or don't run RecyclerView animations");
        }
    }

    private void initChildDimensions(int width, int height, boolean vertical) {
        if (childDimensions[CHILD_WIDTH] != 0 || childDimensions[CHILD_HEIGHT] != 0) {
            // already initialized, skipping
            return;
        }
        if (vertical) {
            childDimensions[CHILD_WIDTH] = width;
            childDimensions[CHILD_HEIGHT] = childSize;
        } else {
            childDimensions[CHILD_WIDTH] = childSize;
            childDimensions[CHILD_HEIGHT] = height;
        }
    }

    @Override
    public void setOrientation(int orientation) {
        // might be called before the constructor of this class is called
        //noinspection ConstantConditions
        if (childDimensions != null) {
            if (getOrientation() != orientation) {
                childDimensions[CHILD_WIDTH] = 0;
                childDimensions[CHILD_HEIGHT] = 0;
            }
        }
        super.setOrientation(orientation);
    }

    public void setFooterSize(int footerSize) {
        if (mFooterSize != footerSize) {
            mFooterSize = footerSize;
            requestLayout();
        }
    }

    private void measureChild(RecyclerView.Recycler recycler, int position, int widthSpec, int heightSpec, int[] dimensions) {
        final View child = recycler.getViewForPosition(position);

        final RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) child.getLayoutParams();

        final int hPadding = getPaddingLeft() + getPaddingRight();
        final int vPadding = getPaddingTop() + getPaddingBottom();

        final int hMargin = p.leftMargin + p.rightMargin;
        final int vMargin = p.topMargin + p.bottomMargin;

        final int hDecoration = getRightDecorationWidth(child) + getLeftDecorationWidth(child);
        final int vDecoration = getTopDecorationHeight(child) + getBottomDecorationHeight(child);

        final int childWidthSpec = getChildMeasureSpec(widthSpec, hPadding + hMargin + hDecoration, p.width, canScrollHorizontally());
        final int childHeightSpec = getChildMeasureSpec(heightSpec, vPadding + vMargin + vDecoration, p.height, canScrollVertically());

        child.measure(childWidthSpec, childHeightSpec);

        dimensions[CHILD_WIDTH] = getDecoratedMeasuredWidth(child) + p.leftMargin + p.rightMargin;
        dimensions[CHILD_HEIGHT] = getDecoratedMeasuredHeight(child) + p.bottomMargin + p.topMargin;

        recycler.recycleView(child);
    }
}
