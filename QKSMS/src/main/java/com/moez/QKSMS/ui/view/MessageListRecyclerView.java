package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.View;
import com.moez.QKSMS.common.utils.UnitUtils;

/**
 * Based off of:
 * https://gist.github.com/JohNan/df776dc4926a1676cc05
 * https://gist.github.com/Teovald/cba0aa150e60b727636d
 */
public class MessageListRecyclerView extends RecyclerView {
    private final String TAG = "MessageListRecyclerView";

    private View mComposeView;
    private int mComposeViewHeight;

    public MessageListRecyclerView(Context context) {
        super(context);
    }

    public MessageListRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setComposeView(View view) {
        mComposeView = view;
        mComposeView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom - top != mComposeViewHeight) {
                mComposeViewHeight = bottom - top;
                int padding = UnitUtils.dpToPx(getContext(), 8);
                setPadding(padding, padding, padding, padding + mComposeViewHeight);

                LayoutManager manager = getLayoutManager();
                if (manager instanceof WrappingLinearLayoutManager) {
                    ((WrappingLinearLayoutManager) manager).setFooterSize(mComposeViewHeight);
                }
            }
        });

        setOnScrollListener(new RecyclerScrollListener());
    }

    public void resetComposeViewPosition() {
        if (mComposeView != null) {
            mComposeView.setTranslationY(0);
        }
    }

    private class RecyclerScrollListener extends OnScrollListener {

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            float translation = mComposeView.getTranslationY();
            translation -= dy;
            if (translation > mComposeViewHeight) {
                translation = mComposeViewHeight;
            } else if (translation < 0) {
                translation = 0;
            }

            mComposeView.setTranslationY(translation);
        }
    }

    private ContextMenu.ContextMenuInfo mContextMenuInfo = null;

    @Override
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
        return mContextMenuInfo;
    }

}
