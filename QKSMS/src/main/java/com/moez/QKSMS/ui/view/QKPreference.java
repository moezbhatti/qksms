package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.preference.Preference;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.moez.QKSMS.R;

/**
 * Regular android preferences don't have basic functionality when you manually add them to views
 * other than preferencegroups, this just cleans up some boilerplate code to set ours up
 */
public class QKPreference extends Preference {

    private OnPreferenceClickListener mOnPreferenceClickListener;

    public QKPreference(Context context, OnPreferenceClickListener onPreferenceClickListener, String key, int title, int summary) {
        super(context);
        mOnPreferenceClickListener = onPreferenceClickListener;

        setKey(key);
        setEnabled(true);
        setLayoutResource(R.layout.list_item_preference);
        if (title != 0) setTitle(title);
        if (summary != 0) setSummary(summary);
    }

    public View getView() {
        return getView(null, null);
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        View view = super.getView(convertView, parent);
        view.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnPreferenceClickListener != null) {
                    mOnPreferenceClickListener.onPreferenceClick(QKPreference.this);
                }
            }
        });

        return view;
    }
}
