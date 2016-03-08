package com.moez.QKSMS.ui.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.SwitchPreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.moez.QKSMS.R;

/**
 * Regular android preferences don't have basic functionality when you manually add them to views
 * other than preferencegroups, this just cleans up some boilerplate code to set ours up
 */
public class QKSwitchPreference extends SwitchPreference {

    private SharedPreferences mPrefs;
    private OnPreferenceClickListener mOnPreferenceClickListener;
    private boolean mDefaultValue;
    private QKSwitch mCheckBox;

    public QKSwitchPreference(Context context, OnPreferenceClickListener onPreferenceClickListener,
                              String key, SharedPreferences prefs, boolean defaultValue, int title, int summary) {
        super(context);
        mPrefs = prefs;
        mOnPreferenceClickListener = onPreferenceClickListener;

        setKey(key);
        setEnabled(true);
        mDefaultValue = prefs.getBoolean(key, defaultValue);
        if (title != 0) setTitle(title);
        if (summary != 0) setSummary(summary);
    }

    public View getView() {
        return getView(null, null);
    }

    @Override
    public View getView(View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_preference, parent, false);
            convertView.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));


            LinearLayout frameLayout = (LinearLayout) convertView.findViewById(android.R.id.widget_frame);
            LayoutInflater.from(getContext()).inflate(R.layout.view_switch, frameLayout);
        }
        super.onBindView(convertView);

        mCheckBox = (QKSwitch) convertView.findViewById(android.R.id.checkbox);
        mCheckBox.setChecked(mDefaultValue);

        convertView.setOnClickListener(v -> {
            mPrefs.edit().putBoolean(getKey(), !mCheckBox.isChecked()).apply();
            mCheckBox.setChecked(!mCheckBox.isChecked());
            if (mOnPreferenceClickListener != null) {
                mOnPreferenceClickListener.onPreferenceClick(QKSwitchPreference.this);
            }
        });

        return convertView;
    }

    @Override
    public boolean isChecked() {
        return mCheckBox == null ? super.isChecked() : mCheckBox.isChecked();
    }
}
