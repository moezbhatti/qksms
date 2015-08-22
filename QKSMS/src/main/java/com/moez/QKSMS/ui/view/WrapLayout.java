package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.moez.QKSMS.R;

public class WrapLayout extends LinearLayout {
    public static final String TAG = "WrapLayout";

    private View mSpace;

    public WrapLayout(Context context) {
        super(context);
    }

    public WrapLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WrapLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onFinishInflate() {
        mSpace = findViewById(R.id.space);
    }

    /**
     * Ask all children to measure themselves and compute the measurement of this
     * layout based on the children.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setOrientation(LinearLayout.HORIZONTAL);
        mSpace.setVisibility(View.GONE);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int contentWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        final int count = getChildCount();

        // Iterate through all children, measuring them and computing our dimensions
        // from their size.
        int totalChildWidth = 0;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                // Measure the child.
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                totalChildWidth += child.getMeasuredWidth();
            }
        }

        if (totalChildWidth > contentWidth) {
            setOrientation(LinearLayout.VERTICAL);
        } else {
            mSpace.setVisibility(View.VISIBLE);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
