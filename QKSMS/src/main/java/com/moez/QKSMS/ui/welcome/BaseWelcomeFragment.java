package com.moez.QKSMS.ui.welcome;

import android.support.v4.view.ViewPager;

import com.moez.QKSMS.ui.base.QKFragment;

public class BaseWelcomeFragment extends QKFragment {

    protected static ViewPager mPager;
    protected static WelcomeActivity mContext;

    public static void setPager(ViewPager pager) {
        mPager = pager;
    }

    public static void setContext(WelcomeActivity context) {
        mContext = context;
    }

    public interface WelcomeScrollListener {
        void onScrollOffsetChanged(WelcomeActivity activity, float offset);
    }

}
