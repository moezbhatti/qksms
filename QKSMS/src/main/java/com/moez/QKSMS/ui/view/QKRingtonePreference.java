package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.media.RingtoneManager;
import android.preference.RingtonePreference;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.moez.QKSMS.R;
import com.moez.QKSMS.enums.QKPreference;

/**
 * Regular android preferences don't have basic functionality when you manually add them to views
 * other than preferencegroups, this just cleans up some boilerplate code to set ours up
 */
public class QKRingtonePreference extends RingtonePreference {

    private OnPreferenceClickListener mOnPreferenceClickListener;

    public QKRingtonePreference(Context context, OnPreferenceClickListener onPreferenceClickListener, QKPreference pref,
                                int title, int summary) {
        super(context);
        mOnPreferenceClickListener = onPreferenceClickListener;

        setKey(pref.getKey());
        setEnabled(true);
        setLayoutResource(R.layout.list_item_preference);
        setRingtoneType(RingtoneManager.TYPE_NOTIFICATION);
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

        view.setOnClickListener(v -> {
            if (mOnPreferenceClickListener != null) {
                mOnPreferenceClickListener.onPreferenceClick(QKRingtonePreference.this);
            }
        });

        return view;
    }
}
