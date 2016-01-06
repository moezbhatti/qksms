package com.moez.QKSMS.ui.welcome;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import com.moez.QKSMS.R;
import com.moez.QKSMS.common.LiveViewManager;
import com.moez.QKSMS.enums.QKPreference;
import com.moez.QKSMS.ui.ThemeManager;
import com.moez.QKSMS.ui.base.QKActivity;
import com.moez.QKSMS.ui.settings.SettingsFragment;
import com.moez.QKSMS.ui.view.RobotoTextView;


public class WelcomeActivity extends QKActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    public static final int WELCOME_REQUEST_CODE = 31415;

    private ViewPager mPager;
    private ImageView mPrevious;
    private ImageView mNext;
    private ImageView[] mIndicators;
    private View mBackground;
    private RobotoTextView mSkip;
    private boolean mFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        getSupportActionBar().hide();

        mBackground = findViewById(R.id.welcome);
        mBackground.setBackgroundColor(ThemeManager.getColor());

        mPrevious = (ImageView) findViewById(R.id.welcome_previous);
        mPrevious.setOnClickListener(this);

        mNext = (ImageView) findViewById(R.id.welcome_next);
        mNext.setOnClickListener(this);

        mSkip = (RobotoTextView) findViewById(R.id.welcome_skip);
        mSkip.setOnClickListener(this);

        mIndicators = new ImageView[]{
                (ImageView) findViewById(R.id.welcome_indicator_0),
                (ImageView) findViewById(R.id.welcome_indicator_1),
                (ImageView) findViewById(R.id.welcome_indicator_2)};
        tintIndicators(0xFFFFFFFF);

        mPager = (ViewPager) findViewById(R.id.welcome_pager);
        BaseWelcomeFragment.setPager(mPager);
        BaseWelcomeFragment.setContext(this);
        mPager.setOnPageChangeListener(this);
        mPager.setAdapter(new WelcomePagerAdapter(getFragmentManager()));

        LiveViewManager.registerView(QKPreference.THEME, this, key -> {
            mBackground.setBackgroundColor(ThemeManager.getColor());
        });
    }

    public void setColorBackground(int color) {
        mBackground.setBackgroundColor(color);
    }

    public void tintIndicators(int color) {
        if (mIndicators != null) {
            for (ImageView indicator : mIndicators) {
                indicator.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        }

        if (mSkip != null) {
            mSkip.setTextColor(color);
        }

        if (mPrevious != null) {
            mPrevious.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }

        if (mNext != null) {
            mNext.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public void setFinished() {
        if (mSkip != null) {
            mFinished = true;
            mSkip.setText(R.string.welcome_finish);
            mSkip.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        Fragment fragment = ((WelcomePagerAdapter) mPager.getAdapter()).getItem(position);
        if (fragment instanceof BaseWelcomeFragment.WelcomeScrollListener) {
            ((BaseWelcomeFragment.WelcomeScrollListener) fragment).onScrollOffsetChanged(this, positionOffset);
        }

        if (position + 1 < mPager.getAdapter().getCount()) {
            Fragment fragment2 = ((WelcomePagerAdapter) mPager.getAdapter()).getItem(position + 1);
            if (fragment2 instanceof BaseWelcomeFragment.WelcomeScrollListener) {
                ((BaseWelcomeFragment.WelcomeScrollListener) fragment2).onScrollOffsetChanged(this, 1 - positionOffset);
            }
        }
    }

    @Override
    public void onPageSelected(int i) {
        if (mIndicators != null) {
            for (ImageView indicator : mIndicators) {
                indicator.setAlpha(0.56f);
            }

            mIndicators[i].setAlpha(1.00f);
        }

        if (mSkip != null) {
            mSkip.setVisibility(i == 0 || mFinished ? View.VISIBLE : View.INVISIBLE);
        }

        if (mPrevious != null) {
            mPrevious.setEnabled(i > 0);
            mPrevious.setAlpha(i > 0 ? 1f : 0.6f);
        }

        if (mNext != null) {
            mNext.setEnabled(i + 1 < mPager.getAdapter().getCount());
            mNext.setAlpha(i + 1 < mPager.getAdapter().getCount() ? 1f : 0.6f);
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.welcome_skip:
                setResult(RESULT_OK, null);
                finish();
                break;
            case R.id.welcome_previous:
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
                break;
            case R.id.welcome_next:
                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
        prefs.putBoolean(SettingsFragment.WELCOME_SEEN, true);
        prefs.apply();
    }
}
