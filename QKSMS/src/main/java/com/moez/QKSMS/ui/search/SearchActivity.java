package com.moez.QKSMS.ui.search;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import com.moez.QKSMS.R;
import com.moez.QKSMS.ui.base.QKActivity;

public class SearchActivity extends QKActivity {

    private SearchFragment mSearchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        showBackButton(true);
        setTitle(R.string.title_search);

        FragmentManager fm = getFragmentManager();
        mSearchFragment = (SearchFragment) fm.findFragmentByTag(SearchFragment.TAG);
        if (mSearchFragment == null) {
            mSearchFragment = new SearchFragment();
        }

        fm.beginTransaction()
                .replace(R.id.content_frame, mSearchFragment, SearchFragment.TAG)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.search, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
