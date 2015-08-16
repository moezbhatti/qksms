package com.moez.QKSMS.ui.welcome;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.util.Log;

public class WelcomePagerAdapter extends FragmentPagerAdapter {
    private final String TAG = "WelcomePagerAdapter";

    private Fragment[] mFragments = new Fragment[3];

    public final int PAGE_INTRO = 0;
    public final int PAGE_THEME = 1;
    public final int PAGE_NIGHT = 2;

    public WelcomePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        if (mFragments[i] == null) {
            switch (i) {
                case PAGE_INTRO:
                    mFragments[i] = new WelcomeIntroFragment();
                    break;
                case PAGE_THEME:
                    mFragments[i] = new WelcomeThemeFragment();
                    break;
                case PAGE_NIGHT:
                    mFragments[i] = new WelcomeNightFragment();
                    break;
                default:
                    Log.e(TAG, "Uh oh, the pager requested a fragment at index " + i + "which doesn't exist");
            }
        }

        return mFragments[i];
    }

    @Override
    public int getCount() {
        return mFragments.length;
    }
}
